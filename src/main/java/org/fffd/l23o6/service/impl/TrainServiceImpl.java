package org.fffd.l23o6.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.mapper.TrainMapper;
import org.fffd.l23o6.mapper.RouteMapper;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.enum_.TrainType;
import org.fffd.l23o6.pojo.vo.route.RouteVO;
import org.fffd.l23o6.pojo.vo.train.AdminTrainVO;
import org.fffd.l23o6.pojo.vo.train.TrainVO;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;
import org.fffd.l23o6.pojo.vo.train.TrainDetailVO;
import org.fffd.l23o6.service.TrainService;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import io.github.lyc8503.spring.starter.incantation.exception.CommonErrorType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {
    private final TrainDao trainDao;
    private final RouteDao routeDao;

    @Override
    public TrainDetailVO getTrain(Long trainId) {
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        return TrainDetailVO.builder().id(trainId).date(train.getDate()).name(train.getName())
                .stationIds(route.getStationIds()).arrivalTimes(train.getArrivalTimes())
                .departureTimes(train.getDepartureTimes()).extraInfos(train.getExtraInfos()).build();
    }

    @Override
    public List<TrainVO> listTrains(Long startStationId, Long endStationId, String date) {
        // TODO
        // First, get all routes contains [startCity, endCity]
        List<RouteEntity> routes = routeDao.findAll();

        Iterator<RouteEntity> iterator1 = routes.iterator();
        while(iterator1.hasNext()){
            RouteEntity r = iterator1.next();
            if(!r.getStationIds().contains(startStationId) || !r.getStationIds().contains(endStationId)){
                iterator1.remove();
            }
        }

        List<Long> routesId = routes.stream().map(RouteEntity::getId).collect(Collectors.toList());
        List<TrainEntity> trains = trainDao.findByRouteIdIn(routesId);

        // Then, Get all trains on that day with the wanted routes
        Iterator<TrainEntity> iterator3 = trains.iterator();
        while(iterator3.hasNext()){
            TrainEntity t = iterator3.next();

            Long routeId = t.getRouteId();
            RouteEntity r = routeDao.findById(routeId).get();
            int startIndex = r.getStationIds().indexOf(startStationId);

            LocalDate expectedDepartureTime = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            Date actualDepartureTime = t.getDepartureTimes().get(startIndex);
            if(!LocalDateTime.ofInstant(actualDepartureTime.toInstant(), ZoneId.systemDefault()).toLocalDate().isEqual(expectedDepartureTime)){
                iterator3.remove();
            }
        }

        List<TrainVO> trainVOS = trains.stream().map(TrainMapper.INSTANCE::toTrainVO).collect(Collectors.toList());
        Iterator<TrainVO> iterator2 = trainVOS.iterator();

        int index = 0;
        while(iterator2.hasNext()){
            Long routeId = trains.get(index).getRouteId();
            RouteEntity r = routeDao.findById(routeId).get();
            int startIndex = r.getStationIds().indexOf(startStationId);
            int endIndex = r.getStationIds().indexOf(endStationId);

            List<TicketInfo> ticketInfos = new ArrayList<>();

            TrainVO t = iterator2.next();
            switch (trains.get(index).getTrainType()) {
                case HIGH_SPEED:
                    Map<GSeriesSeatStrategy.GSeriesSeatType, Integer> GleftSeat = GSeriesSeatStrategy.INSTANCE.getLeftSeatCount(startIndex, endIndex, trains.get(index).getSeats());

                    for(Map.Entry<GSeriesSeatStrategy.GSeriesSeatType, Integer> entry : GleftSeat.entrySet()){
                        GSeriesSeatStrategy.GSeriesSeatType type = entry.getKey();
                        Integer count = entry.getValue();
                        ticketInfos.add(new TicketInfo(type.getText(), count, 100));
                    }
                    break;
                case NORMAL_SPEED:
                    Map<KSeriesSeatStrategy.KSeriesSeatType, Integer> KleftSeat = KSeriesSeatStrategy.INSTANCE.getLeftSeatCount(startIndex, endIndex, trains.get(index).getSeats());

                    for(Map.Entry<KSeriesSeatStrategy.KSeriesSeatType, Integer> entry : KleftSeat.entrySet()){
                        KSeriesSeatStrategy.KSeriesSeatType type = entry.getKey();
                        Integer count = entry.getValue();
                        ticketInfos.add(new TicketInfo(type.getText(), count, 100));
                    }
                    break;
            }
            t.setTicketInfo(ticketInfos);
            t.setStartStationId(startStationId);
            t.setEndStationId(endStationId);
            t.setDepartureTime(trains.get(index).getDepartureTimes().get(startIndex));
            t.setArrivalTime(trains.get(index).getArrivalTimes().get(endIndex));
            index++;
        }
        return trainVOS;

    }

    @Override
    public List<AdminTrainVO> listTrainsAdmin() {
        return trainDao.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(TrainMapper.INSTANCE::toAdminTrainVO).collect(Collectors.toList());
    }

    @Override
    public void addTrain(String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
                         List<Date> departureTimes) {
        TrainEntity entity = TrainEntity.builder().name(name).routeId(routeId).trainType(type)
                .date(date).arrivalTimes(arrivalTimes).departureTimes(departureTimes).build();
        RouteEntity route = routeDao.findById(routeId).get();
        boolean time_valid = true;
        for(int i = 0; i < entity.getArrivalTimes().size(); i++){
            if(entity.getArrivalTimes().get(i) == null){
                time_valid = false;
                break;
            }
        }
        for(int i = 0; i < entity.getDepartureTimes().size(); i++){
            if(entity.getDepartureTimes().get(i) == null){
                time_valid = false;
                break;
            }
        }
        if (route.getStationIds().size() != entity.getArrivalTimes().size()
                || route.getStationIds().size() != entity.getDepartureTimes().size()
                || !time_valid) {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "列表长度错误");
        }

        String[] month = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] date_split = date.split("-");
        String[] arrival_split = entity.getArrivalTimes().get(0).toString().split(" ");
        if(!date_split[0].equals(arrival_split[5])
                || !month[Integer.parseInt(date_split[1]) - 1].equals(arrival_split[1])
                || !date_split[2].equals(arrival_split[2])){
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "出发日期不匹配");
        }

        for(int i = 0; i < entity.getArrivalTimes().size() - 1; i++){
            if(entity.getArrivalTimes().get(i).after(entity.getArrivalTimes().get(i + 1))){
                throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "时间错误");
            }
        }
        for(int i = 0; i < entity.getDepartureTimes().size() - 1; i++){
            if(entity.getDepartureTimes().get(i).after(entity.getDepartureTimes().get(i + 1))){
                throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "时间错误");
            }
        }
        for(int i = 0; i < entity.getArrivalTimes().size(); i++){
            if(entity.getArrivalTimes().get(i).after(entity.getDepartureTimes().get(i))){
                throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "时间错误");
            }
        }

        entity.setExtraInfos(new ArrayList<String>(Collections.nCopies(route.getStationIds().size(), "预计正点")));
        switch (entity.getTrainType()) {
            case HIGH_SPEED:
                entity.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
            case NORMAL_SPEED:
                entity.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
        }
        trainDao.save(entity);
    }

    @Override
    public void changeTrain(Long id, String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
                            List<Date> departureTimes) {

        TrainEntity train = trainDao.findById(id).get();
        if (train == null){
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "车次不存在");
        }
        train.setName(name);
        train.setRouteId(routeId);
        train.setTrainType(type);
        train.setDate(date);
        train.setArrivalTimes(arrivalTimes);
        train.setDepartureTimes(departureTimes);

        RouteEntity route = routeDao.findById(routeId).get();
        boolean time_valid = true;
        for(int i = 0; i < train.getArrivalTimes().size(); i++){
            if(train.getArrivalTimes().get(i) == null){
                time_valid = false;
                break;
            }
        }
        for(int i = 0; i < train.getDepartureTimes().size(); i++){
            if(train.getDepartureTimes().get(i) == null){
                time_valid = false;
                break;
            }
        }
        if (route.getStationIds().size() != train.getArrivalTimes().size()
                || route.getStationIds().size() != train.getDepartureTimes().size()
                || !time_valid) {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "列表长度错误");
        }

        String[] month = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] date_split = date.split("-");
        String[] arrival_split = train.getArrivalTimes().get(0).toString().split(" ");
        if(!date_split[0].equals(arrival_split[5])
                || !month[Integer.parseInt(date_split[1]) - 1].equals(arrival_split[1])
                || !date_split[2].equals(arrival_split[2])){
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "出发日期不匹配");
        }

        for(int i = 0; i < train.getArrivalTimes().size() - 1; i++){
            if(train.getArrivalTimes().get(i).after(train.getArrivalTimes().get(i + 1))){
                throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "时间错误");
            }
        }
        for(int i = 0; i < train.getDepartureTimes().size() - 1; i++){
            if(train.getDepartureTimes().get(i).after(train.getDepartureTimes().get(i + 1))){
                throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "时间错误");
            }
        }
        for(int i = 0; i < train.getArrivalTimes().size(); i++){
            if(train.getArrivalTimes().get(i).after(train.getDepartureTimes().get(i))){
                throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "时间错误");
            }
        }

        train.setExtraInfos(new ArrayList<String>(Collections.nCopies(route.getStationIds().size(), "预计正点")));

        switch (train.getTrainType()) {
            case HIGH_SPEED:
                train.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
            case NORMAL_SPEED:
                train.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                break;
        }
        trainDao.save(train);

    }
    @Override
    public void deleteTrain(Long id) {
        trainDao.deleteById(id);
    }
}
