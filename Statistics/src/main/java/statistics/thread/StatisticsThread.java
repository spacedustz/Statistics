package statistics.thread;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import statistics.constants.RedisConstants;
import statistics.service.StatisticsService;
import statistics.service.redis.RedisService;
import statistics.util.DateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
public class StatisticsThread extends Thread {
    private final RedisService redisService;
    private final StatisticsService statisticsService;
    private final List<String> keyList;
    private final Integer squareMeter = new Random().nextInt(21) + 20;

    public StatisticsThread(List<String> keyList, RedisService redisService, StatisticsService statisticsService) {
        this.keyList = keyList;
        this.redisService = redisService;
        this.statisticsService = statisticsService;
    }

    @Override
    public void run() {
        for (String key : keyList) {
            log.info("================= 키 리스트 사이즈 : {} ==================", keyList.size());
            Set<String> hashKeyToDelete = new HashSet<>();
            Stats stats = new Stats();

            String routingKey = key.substring(RedisConstants.INSTANCE_COUNT.length());
            String eventTime;
            int eventSecInt;
            int totalSquareMeter = squareMeter;
            String baseTime = "";

            try {
                // 특정 Hash의 모든 Hash Key, Hash Value를 가져와 TreeMap으로 변환 - 키 기준 정렬
                Map<String, Integer> hashValues = redisService.getAllHashKeyAndValues(key);
                if (hashValues.isEmpty()) return;

                TreeMap<String, Integer> sortedHash = new TreeMap<>(hashValues);
                if (sortedHash.isEmpty()) return;

                // 정렬된 Hash 필드와 값의 각 엔트리 Loop
                List<Map.Entry<String, Integer>> entryList = new ArrayList<>(sortedHash.entrySet());

                resetStats(stats);

                // 기준이 되는 시간 (60초)
                long validStatTime = Long.parseLong(DateUtil.getDateTime(DateUtil.addSeconds(new Date(), -60)));

                for (int x = 0; x < entryList.size(); x++) {
                    Map.Entry<String, Integer> entry = entryList.get(x);
                    // 삭제할 Hash Key 추가
                    hashKeyToDelete.add(entry.getKey());

                    eventTime = entry.getKey();
                    eventSecInt = Integer.parseInt(eventTime.substring(12, 14));
                    stats.setValue(entry.getValue());

                    // Hash Key(Event Time)이 유호시간(60초)보다 이전이거나 값이 0이면 통계 계산을 하지 않고 건너뜀
                    if (Long.parseLong(eventTime) < validStatTime || stats.getValue() == 0) continue;

                    String eventSecond = getEventSec(eventSecInt);
                    String newBaseTime = eventTime.substring(0, 12) + eventSecond;

                    // 첫번쨰 Entry이면서 마지막 엔트리가 아닐때, 즉 전체 Entry 개수가 1이 아니면서 첫번째 데이터인 경우
                    if (x == 0 && entryList.size() != 1) {
                        updateStatsForFirstData(stats);
                    }
                    // 마지막 데이터가 아닌경우
                    else if (x + 1 != entryList.size()) {
//                        baseTime = processStatsByTimeRange(eventSecInt, eventTime, stats, totalSquareMeter, routingKey, baseTime);

                        if (baseTime.equals(eventTime.substring(0, 12) + getEventSec(eventSecInt))) {
                            updateStats(stats);
                        } else {
                            if (stats.getCount() > 0) saveStats(stats, totalSquareMeter, routingKey, baseTime);
                            resetStats(stats);
                            baseTime = newBaseTime;
                            updateStats(stats);
                        }
                    }
                    // 마지막 데이터인 경우
                    else if (x + 1 == entryList.size()) {
//                        baseTime = processStatsByTimeRangeForLastData(eventSecInt, eventTime, stats, totalSquareMeter, routingKey, baseTime);

                        if (baseTime.equals(newBaseTime)) {
                            if (eventSecInt >= 1 && eventSecInt < 16) updateStatsForLastData(stats); else updateStats(stats);
                            if (stats.getCount() > 0) saveStats(stats, totalSquareMeter, routingKey, baseTime);
                        } else {
                            updateStats(stats);
                            baseTime = newBaseTime;
                            if (stats.getCount() > 0) saveStats(stats, totalSquareMeter, routingKey, baseTime);
                            resetStats(stats);
                        }
                    }
                } // For Loop
            } catch (Exception e) {
                log.error("통계 Thread Exception - {}", e.getMessage());
                e.printStackTrace();
            } finally {
                // 평균 값을 구하는데 사용한 데이터를 Redis에서 제거
                if (!hashKeyToDelete.isEmpty()) {
                    redisService.deleteHashKeys(key, hashKeyToDelete.toArray());
                    log.info("{} - Delete Hash Keys", routingKey);
                }
            }
        }
    }

    private String getEventSec(int eventSecInt) {
        if (eventSecInt >= 1 && eventSecInt < 16) {
            return "15";
        } else if (eventSecInt >= 16 && eventSecInt < 31) {
            return "30";
        } else if (eventSecInt >= 31 && eventSecInt < 46) {
            return "45";
        } else if (eventSecInt >= 46 || eventSecInt == 0) {
            return "00";
        } else {
            return "";
        }
    }

    private void resetStats(Stats stats) {
        stats.initProperties();
    }

    private void updateStatsForFirstData(Stats stats) {
        stats.setSum(stats.getSum() + stats.getValue());
        stats.setCount(stats.getCount() + 1);
        stats.setMax(stats.getValue());
        stats.setMin(stats.getValue());
        log.info("First Min = {}", stats.getMin());
    }

    private void updateStats(Stats stats) {
        int max = stats.getMax();
        int min = stats.getMin();
        int value = stats.value;

        if (max < value) stats.setMax(value);
        if (min > value) stats.setMin(value);
        stats.setSum(stats.getSum() + stats.getValue());
        stats.setCount(stats.getCount() + 1);
        log.info("Middle Min = {}", stats.getMin());
    }

    private void updateStatsForLastData(Stats stats) {
        stats.setMax(stats.getValue());
        stats.setMin(stats.getValue());
        stats.setSum(stats.getSum() + stats.getValue());
        stats.setCount(stats.getCount() + 1);
        log.info("Last Min = {}", stats.getMin());
    }

    private void saveStats(Stats stats, int totalSquareMeter, String routingKey, String baseTime) {
            // 평균값 계산
            synchronized (StatisticsThread.class) {
                BigDecimal average = BigDecimal.valueOf(stats.getSum())
                        .divide(BigDecimal.valueOf(stats.getCount()), 1, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP);

                long now = Long.parseLong(DateUtil.getTime());

                if (baseTime.isEmpty()) {
                    log.info("baseTime의 값이 없습니다.");
                    return;
                }


                if (Long.parseLong(baseTime) > now) {
                    log.info("미래의 시간입니다.");
                    return;
                }
                // DB에 저장
                try {
                    statisticsService.save15SecAvgToDB(
                            baseTime.substring(0, 8),
                            baseTime.substring(8, 14),
                            routingKey,
                            average,
                            new BigDecimal(stats.getMax()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP),
                            new BigDecimal(stats.getMin()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP)
                    );

                    log.info("15초 통계 저장 - min 값 : {}", new BigDecimal(stats.getMin()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP));
                } catch (Exception e) {
                    log.error("15초 통계 저장 실패 - {}", e.getMessage());
                    e.printStackTrace();
                }
            }

    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class Stats {
        int count = 0;
        int sum = 0;
        int max = 0;
        int min = Integer.MAX_VALUE;
        int value = 0;

        public void initProperties() {
            this.count = 0;
            this.sum = 0;
            this.max = 0;
            this.min = 0;
            this.value = 0;
        }
    }
}
