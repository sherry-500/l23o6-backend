package org.fffd.l23o6.util.strategy.train;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nullable;


public class GSeriesSeatStrategy extends TrainSeatStrategy {
    public static final GSeriesSeatStrategy INSTANCE = new GSeriesSeatStrategy();
     
    private final Map<Integer, String> BUSINESS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> FIRST_CLASS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> SECOND_CLASS_SEAT_MAP = new HashMap<>();

    private final Map<GSeriesSeatType, Map<Integer, String>> TYPE_MAP = new HashMap<>() {{
        put(GSeriesSeatType.BUSINESS_SEAT, BUSINESS_SEAT_MAP);
        put(GSeriesSeatType.FIRST_CLASS_SEAT, FIRST_CLASS_SEAT_MAP);
        put(GSeriesSeatType.SECOND_CLASS_SEAT, SECOND_CLASS_SEAT_MAP);
    }};


    private GSeriesSeatStrategy() {
        //高铁策略
        int counter = 0;

        for (String s : Arrays.asList("1车1A","1车1C","1车1F")) {
            BUSINESS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("2车1A","2车1C","2车1D","2车1F","2车2A","2车2C","2车2D","2车2F","3车1A","3车1C","3车1D","3车1F")) {
            FIRST_CLASS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("4车1A","4车1B","4车1C","4车1D","4车2F","4车2A","4车2B","4车2C","4车2D","4车2F","4车3A","4车3B","4车3C","4车3D","4车3F")) {
            SECOND_CLASS_SEAT_MAP.put(counter++, s);
        }
        
    }

    public enum GSeriesSeatType implements SeatType {
        BUSINESS_SEAT("商务座"), FIRST_CLASS_SEAT("一等座"), SECOND_CLASS_SEAT("二等座"), NO_SEAT("无座");
        private String text;
        GSeriesSeatType(String text){
            this.text=text;
        }
        public String getText() {
            return this.text;
        }
        public static GSeriesSeatType fromString(String text) {
            for (GSeriesSeatType b : GSeriesSeatType.values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }


    public @Nullable String allocSeat(int startStationIndex, int endStationIndex, GSeriesSeatType type, boolean[][] seatMap) {
        //endStationIndex - 1 = upper bound
        int lower_bound = 0;
        int upper_bound = 0;
        if (type.getText().equals("商务座")){
            upper_bound = BUSINESS_SEAT_MAP.size();
        }else if (type.getText().equals("一等座")){
            lower_bound = BUSINESS_SEAT_MAP.size();
            upper_bound = lower_bound +  FIRST_CLASS_SEAT_MAP.size();
        }else if (type.getText().equals("二等座")){
            lower_bound = BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size();
            upper_bound = lower_bound + SECOND_CLASS_SEAT_MAP.size();
        }

        //所需类型的车票在seatMap中的下界
        int i = lower_bound;
        for(; i < upper_bound; i++){
            int j = startStationIndex;
            for(; j < endStationIndex; j++){
                if(seatMap[j][i]){
                    break;
                }
            }
            if(j == endStationIndex){
                j = startStationIndex;
                for(; j < endStationIndex; j++){
                    seatMap[j][i] = true;
                }
                break;
            }
        }
        String result = TYPE_MAP.get(type).get(i);
        return result;
    }

    public Map<GSeriesSeatType, Integer> getLeftSeatCount(int startStationIndex, int endStationIndex, boolean[][] seatMap) {

        Map<GSeriesSeatStrategy.GSeriesSeatType, Integer> leftSeat = new HashMap<GSeriesSeatStrategy.GSeriesSeatType, Integer>();
        int businessSeatCount = BUSINESS_SEAT_MAP.size();
        int firstClassSeatCount = FIRST_CLASS_SEAT_MAP.size();
        int secondClassSeatCount = SECOND_CLASS_SEAT_MAP.size();

        for(int j = 0; j < BUSINESS_SEAT_MAP.size(); j++){
            for(int i = startStationIndex; i < endStationIndex; i++) {
                if (seatMap[i][j]) {
                    businessSeatCount--;
                    break;
                }
            }
        }

        for(int j = BUSINESS_SEAT_MAP.size(); j < BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size(); j++){
            for(int i = startStationIndex; i < endStationIndex; i++) {
                if (seatMap[i][j]) {
                    firstClassSeatCount--;
                    break;
                }
            }
        }

        for(int j = BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size() ; j < BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size() + SECOND_CLASS_SEAT_MAP.size(); j++){
            for(int i = startStationIndex; i < endStationIndex; i++) {
                if (seatMap[i][j]) {
                    secondClassSeatCount--;
                    break;
                }
            }
        }

        leftSeat.put(GSeriesSeatType.BUSINESS_SEAT, businessSeatCount);
        leftSeat.put(GSeriesSeatType.FIRST_CLASS_SEAT, firstClassSeatCount);
        leftSeat.put(GSeriesSeatType.SECOND_CLASS_SEAT, secondClassSeatCount);

        return leftSeat;
    }

    public boolean[][] initSeatMap(int stationCount) {
        return new boolean[stationCount - 1][BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size() + SECOND_CLASS_SEAT_MAP.size()];
    }
}
