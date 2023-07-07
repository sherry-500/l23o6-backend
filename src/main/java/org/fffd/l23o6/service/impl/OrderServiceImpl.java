package org.fffd.l23o6.service.impl;

import java.security.AlgorithmConstraints;
import java.util.List;
import java.util.stream.Collectors;

import org.fffd.l23o6.dao.OrderDao;
import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.dao.UserDao;
import org.fffd.l23o6.pojo.entity.UserEntity;
import org.fffd.l23o6.pojo.enum_.OrderStatus;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.vo.order.OrderVO;
import org.fffd.l23o6.service.OrderService;
import org.fffd.l23o6.util.strategy.payment.AliPayPaymentStrategy;
import org.fffd.l23o6.util.strategy.payment.MileagePointPaymentStrategy;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final TrainDao trainDao;
    private final RouteDao routeDao;

    public Long createOrder(String username, Long trainId, Long fromStationId, Long toStationId, String seatType,
                            Long seatNumber) {
        Long userId = userDao.findByUsername(username).getId();
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        int startStationIndex = route.getStationIds().indexOf(fromStationId);
        int endStationIndex = route.getStationIds().indexOf(toStationId);
        String seat = null;
        switch (train.getTrainType()) {
            case HIGH_SPEED:
                seat = GSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                        GSeriesSeatStrategy.GSeriesSeatType.fromString(seatType), train.getSeats());
                break;
            case NORMAL_SPEED:
                seat = KSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                        KSeriesSeatStrategy.KSeriesSeatType.fromString(seatType), train.getSeats());
                break;
        }
        if (seat == null) {
            throw new BizException(BizError.OUT_OF_SEAT);
        }
        OrderEntity order = OrderEntity.builder().trainId(trainId).userId(userId).seat(seat)
                .status(OrderStatus.PENDING_PAYMENT).arrivalStationId(toStationId).departureStationId(fromStationId)
                .price(100).payPrice(0).build();
        train.setUpdatedAt(null);// force it to update
        trainDao.save(train);
        orderDao.save(order);


        return order.getId();
    }

    public List<OrderVO> listOrders(String username) {
        Long userId = userDao.findByUsername(username).getId();
        List<OrderEntity> orders = orderDao.findByUserId(userId);
        orders.sort((o1,o2)-> o2.getId().compareTo(o1.getId()));
        return orders.stream().map(order -> {
            TrainEntity train = trainDao.findById(order.getTrainId()).get();
            RouteEntity route = routeDao.findById(train.getRouteId()).get();
            int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
            int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
            return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                    .seat(order.getSeat()).status(order.getStatus().getText())
                    .createdAt(order.getCreatedAt())
                    .startStationId(order.getDepartureStationId())
                    .endStationId(order.getArrivalStationId())
                    .departureTime(train.getDepartureTimes().get(startIndex))
                    .arrivalTime(train.getArrivalTimes().get(endIndex))
                    .build();
        }).collect(Collectors.toList());
    }

    public OrderVO getOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        TrainEntity train = trainDao.findById(order.getTrainId()).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
        int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
        return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                .seat(order.getSeat()).status(order.getStatus().getText())
                .createdAt(order.getCreatedAt())
                .startStationId(order.getDepartureStationId())
                .endStationId(order.getArrivalStationId())
                .departureTime(train.getDepartureTimes().get(startIndex))
                .arrivalTime(train.getArrivalTimes().get(endIndex)).price(order.getPrice())
                .build();
    }

    public void cancelOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        UserEntity user = userDao.findById(order.getUserId()).get();

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }

        // TODO: refund user's money and credits if needed
        //用户实际支付的金额，需要退还
        double payPrice = order.getPayPrice();
        if(order.getStatus() == OrderStatus.PAID){
            //退还支付金额
            AliPayPaymentStrategy.INSTANCE.refund(order.getId(), payPrice);
        }else if(order.getStatus() == OrderStatus.OPAID){
            //用户消耗的积分，需要返还（若未花钱则证明是积分支付）;默认10倍的积分抵消票价
            int mileAgePoint = order.getPrice() * 10;
            user.setMileagePoints(user.getMileagePoints() + mileAgePoint);
        }

        order.setPayPrice(0);
        order.setStatus(OrderStatus.CANCELLED);
        orderDao.save(order);

    }

    public String payOrder(Long id, int payMethod) {
        String result = null;
        //payMethod = 0,其他方式支付; payMethod = 1,支付宝支付
        OrderEntity order = orderDao.findById(id).get();
        UserEntity user = userDao.findById(order.getUserId()).get();

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }

        //use payment strategy to pay!
        //update user's credits, so that user can get discount next time

        if (payMethod == 0){
            //使用积分支付则不能使用积分优惠，积分不足则支付失败
            double amount = MileagePointPaymentStrategy.INSTANCE.calculateAmount((int)order.getPrice(), user.getMileagePoints());
            result = MileagePointPaymentStrategy.INSTANCE.pay(id, order.getSeat(), amount);

            user.setMileagePoints(user.getMileagePoints() - (int)amount);
            order.setPayPrice(0);
            userDao.save(user);
            order.setStatus(OrderStatus.OPAID);
        }else {
            //使用支付宝支付则自动计算积分优惠，使用优惠不扣除相应积分（类似于会员等级）
            //实际支付的前按10:1的比例计算为积分
            double amount = AliPayPaymentStrategy.INSTANCE.calculateAmount((int)order.getPrice(), user.getMileagePoints());
            result = AliPayPaymentStrategy.INSTANCE.pay(id, order.getSeat(), amount);

            user.setMileagePoints(user.getMileagePoints() + (int)amount / 10);
            order.setPayPrice(amount);
            userDao.save(user);
            order.setStatus(OrderStatus.PAID);
        }

        orderDao.save(order);

        return result;
    }

}
