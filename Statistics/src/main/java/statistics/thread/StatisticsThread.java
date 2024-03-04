package statistics.thread;

import lombok.extern.slf4j.Slf4j;
import statistics.constants.RedisConstants;
import statistics.service.StatisticsService;
import statistics.service.redis.RedisService;
import statistics.util.DateUtil;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
public class StatisticsThread extends Thread {
    private RedisService redisService;
    private StatisticsService statisticsService;
    private List<String> keyList;
    private Integer squareMeter = new Random().nextInt(21)+ 10;
    private String baseTime = "", routingKey = "", eventTime = "", eventSec = "";
    private int eventSecInt = 0, totalSquareMeter = squareMeter, sum = 0, count = 0, max = 0, min = 0, value = 0;
    private BigDecimal average = BigDecimal.ZERO;
    private BigDecimal averageLog = BigDecimal.ZERO;

    public StatisticsThread(List<String> keyList, RedisService redisService, StatisticsService statisticsService) {
        this.keyList = keyList;
        this.redisService = redisService;
        this.statisticsService = statisticsService;
    }

    @Override
    public void run() {
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.get(i);
            Set<String> hashKeyToDelete = new HashSet<>();
            routingKey = key.substring(RedisConstants.INSTANCE_COUNT.length());

            try {
                // 특정 Hash의 모든 Hash Key, Hash Value를 가져와 TreeMap으로 변환 - 키 기준 정렬
                Map<String, Integer> hashValues = redisService.getAllHashKeyAndValues(key);
                if (hashValues.isEmpty()) return;

                TreeMap<String, Integer> sortedHash = new TreeMap<>(hashValues);
                if (sortedHash.isEmpty()) return;

                resetStats();

                // 기준이 되는 시간 (60초)
                long validStatTime = Long.parseLong(DateUtil.getDateTime(DateUtil.addSeconds(new Date(), -60)));

                // 정렬된 Hash 필드와 값의 각 엔트리 Loop
                List<Map.Entry<String, Integer>> entryList = new ArrayList<>(sortedHash.entrySet());

                for (int x = 0; x < entryList.size(); x++) {
                    Map.Entry<String, Integer> entry = entryList.get(x);
                    // 삭제할 Hash Key 추가
                    hashKeyToDelete.add(entry.getKey());

                    eventTime = entry.getKey();
                    value = entry.getValue();
                    eventSec = eventTime.substring(12, 14);
                    eventSecInt = Integer.parseInt(eventSec);

                    // Hash Key(Event Time)이 유호시간(60초)보다 이전이거나 값이 0이면 통계 계산을 하지 않고 건너뜀
                    if (Long.parseLong(eventTime) < validStatTime || value == 0) continue;

                    // 첫번쨰 Entry이면서 마지막 엔트리가 아닐때, 즉 전체 Entry 개수가 1이 아니면서 첫번째 데이터인 경우
                    if (x == 0 && entryList.size() != 1) {
                        baseTime = getBaseTime(eventSecInt, eventTime);

                        sum += value;
                        count++;
                        max = value;
                        min = value;
                        // 마지막 데이터가 아닌경우
                    } else if (x + 1 != entryList.size()) {
                        processStatsByTimeRange(eventSecInt, baseTime, eventTime, value, count, max, min, false);
                        // 마지막 데이터인 경우
                    } else if (x + 1 == entryList.size()) {
                        processStatsByTimeRange(eventSecInt, baseTime, eventTime, value, count, max, min, true);
                    }
                } // For Loop

                log.info("{} - 통계 인원수 평균값 : {}, 최소값 : {}, 최대값 : {}", routingKey, averageLog.toString(), min, max);
                this.averageLog = BigDecimal.ZERO;
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

    /* Event Time의 Second에 따라 기준 Second 반환 */
    private String getBaseTime(int eventSecInt, String eventTime) {
        if (eventSecInt >= 1 && eventSecInt < 16) {
            return eventTime.substring(0, 12) + "15";
        } else if (eventSecInt >= 16 && eventSecInt < 31) {
            return eventTime.substring(0, 12) + "30";
        } else if (eventSecInt >= 31 && eventSecInt < 46) {
            return eventTime.substring(0, 12) + "45";
        } else if (eventSecInt >= 46 || eventSecInt == 0) {
            return eventTime.substring(0, 12) + "00";
        } else {
            return "";
        }
    }

    private void resetStats() {
        count = 0;
        sum = 0;
        max = 0;
        min = 0;
        value = 0;
    }

    private void updateStats(int value, int max, int min) {
        if (max < value) this.max = value;
        if (min > value) this.min = value;
        this.sum += value;
        this.count++;
    }

    private void updateLastStats(int value) {
        this.sum += value;
        this.count++;
    }

    private void saveStats(int count) {
        if (count > 0) {
            // 평균값 계산
            this.average = BigDecimal.valueOf(this.sum)
                    .divide(BigDecimal.valueOf(this.count), 3, BigDecimal.ROUND_HALF_UP)
                    .divide(BigDecimal.valueOf(this.totalSquareMeter), 3, BigDecimal.ROUND_HALF_UP);

            // DB에 저장
            statisticsService.save15SecAvgToDB(
                    baseTime.substring(0, 8),
                    baseTime.substring(8, 14),
                    routingKey,
                    average,
                    new BigDecimal(this.max).divide(BigDecimal.valueOf(this.totalSquareMeter), 3, BigDecimal.ROUND_HALF_UP),
                    new BigDecimal(this.min).divide(BigDecimal.valueOf(this.totalSquareMeter), 3, BigDecimal.ROUND_HALF_UP)
            );

            log.info("Save Statistics Data to MariaDB");

            this.averageLog = average;
            this.average = BigDecimal.ZERO;
            log.info("Initialize Average");
        }
    }

    private void processStatsByTimeRange(int eventSecInt, String baseTime, String eventTime, int value, int count, int max, int min, boolean isLast) {
        String timeRange = "";
        if (eventSecInt >= 1 && eventSecInt < 16) {
            timeRange = "15";
        } else if (eventSecInt >= 16 && eventSecInt < 31) {
            timeRange = "30";
        } else if (eventSecInt >= 31 && eventSecInt < 46) {
            timeRange = "45";
        } else if (eventSecInt >= 46 || eventSecInt == 0) {
            timeRange = "00";
        }

        if (isLast) {
            processLastStats(baseTime, eventTime, value, count, max, min, timeRange);
        } else {
            processMiddleStats(baseTime, eventTime, value, count, max, min, timeRange);
        }
    }

    private void processMiddleStats(String baseTime, String eventTime, int value, int count, int max, int min, String timeRange) {
        if (baseTime.equals(eventTime.substring(0, 12) + timeRange)) {
            updateStats(value, max, min);
        } else {
            saveStats(count);
            resetStats();
            setBaseTime(eventTime, timeRange);
            updateStats(value, max, min);
        }
    }

    private void processLastStats(String baseTime, String eventTime, int value, int count, int max, int min, String timeRange) {
        if (baseTime.equals(eventTime.substring(0, 12) + timeRange)) {
            if (timeRange.equals("15")) updateLastStats(value); else updateStats(value, max, min);
            saveStats(count);
        } else {
            updateStats(value, max, min);
            setBaseTime(eventTime, timeRange);
            saveStats(count);
            resetStats();
        }
    }

    private void setBaseTime(String eventTime, String second) {
        baseTime = eventTime.substring(0,12) + second;
    }
}
