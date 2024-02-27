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
    private Integer squareMeter = new Random().nextInt(21)+ 30;
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

                log.info("========== \uD83D\uDCC4 {} 통계 처리 시작 ==========", routingKey);
                log.info("기준 시간 = {}", validStatTime);

                // 정렬된 Hash 필드와 값의 각 엔트리 Loop
                List<Map.Entry<String, Integer>> entryList = new ArrayList<>(sortedHash.entrySet());
                log.info("Hash 수 : {}", entryList.size());

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
                        processStatsByTimeRange(eventSecInt, baseTime, eventTime, value, count, sum, totalSquareMeter, max, min);
                        // 마지막 데이터인 경우
                    } else if (x + 1 == entryList.size()) {
                        processStatsByTimeRange2(eventSecInt, baseTime, eventTime, value, count, sum, totalSquareMeter, max, min);
                    }
                } // For Loop

                log.info("통계 인원수 평균값 : {}, 최소값 : {}, 최대값 : {}", averageLog.toString(), min, max);
                this.averageLog = BigDecimal.ZERO;

                log.info("통계 처리 완료");
            } catch (Exception e) {
                log.error("통계 Thread Exception - {}", e.getMessage());
                e.printStackTrace();
            } finally {
                // 평균 값을 구하는데 사용한 데이터를 Redis에서 제거
                if (!hashKeyToDelete.isEmpty()) {
                    redisService.deleteHashKeys(key, hashKeyToDelete.toArray());
                    log.info("Statistics Thread - Delete Hash Keys");
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

    private void updateStats2(int value) {
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

    private void processStatsByTimeRange(int eventSecInt, String baseTime, String eventTime, int value, int count, int sum, int totalSquareMeter, int max, int min) {
        // 01 ~ 15초 통계 - baseTime이 15초와 동일하면 변수 업데이트 & DB 저장
        if (eventSecInt >= 1 && eventSecInt < 16) {
            if (baseTime.equals(eventTime.substring(0, 12) + "15")) {
                updateStats(value, max, min);
            } else {
                saveStats(count);
                resetStats();
                setBaseTime(eventTime, "15");
                updateStats(value, max, min);

            }
        }
        // 16 ~ 30초 통계 - baseTime이 30초와 동일하면 변수 업데이트 & DB 저장
        else if (eventSecInt >= 16 && eventSecInt < 31) {
            if (baseTime.equals(eventTime.substring(0, 12) + "30")) {
                updateStats(value, max, min);
            } else {
                saveStats(count);
                resetStats();
                setBaseTime(eventTime, "30");
                updateStats(value, max, min);
            }
        }

        // 31 ~ 45초 통계 - baseTime이 45초와 동일하면 변수 업데이트 & DB 저장
        else if (eventSecInt >= 31 && eventSecInt < 46) {
            if (baseTime.equals(eventTime.substring(0, 12) + "45")) {
                updateStats(value, max, min);
            } else {
                saveStats(count);
                resetStats();
                setBaseTime(eventTime, "45");
                updateStats(value, max, min);
            }
        }

        // 45 ~ 00초 통계 - baseTime이 00초와 동일하면 변수 업데이트 & DB 저장
        else if (eventSecInt >= 46 || eventSecInt == 0) {
            if (baseTime.equals(eventTime.substring(0, 12) + "00")) {
                updateStats(value, max, min);
            } else {
                saveStats(count);
                resetStats();
                setBaseTime(eventTime, "00");
                updateStats(value, max, min);
            }
        }
    }

    private void processStatsByTimeRange2(int eventSecInt, String baseTime, String eventTime, int value, int count, int sum, int totalSquareMeter, int max, int min) {
        // 01 ~ 15초 통계 - baseTime이 15초와 동일하면 변수 업데이트 & DB 저장
        if (eventSecInt >= 1 && eventSecInt < 16) {
            if (baseTime.equals(eventTime.substring(0, 12) + "15")) {
                updateStats2(value);
                saveStats(count);
            } else {
                updateStats(value, max, min);
                setBaseTime(eventTime, "15");
                saveStats(count);
                resetStats();
            }
        }
        // 16 ~ 30초 통계 - baseTime이 30초와 동일하면 변수 업데이트 & DB 저장
        else if (eventSecInt >= 16 && eventSecInt < 31) {
            if (baseTime.equals(eventTime.substring(0, 12) + "30")) {
                updateStats(value, max, min);
                saveStats(count);
            } else {
                updateStats(value, max, min);
                setBaseTime(eventTime, "30");
                saveStats(count);
                resetStats();
            }
        }

        // 31 ~ 45초 통계 - baseTime이 45초와 동일하면 변수 업데이트 & DB 저장
        else if (eventSecInt >= 31 && eventSecInt < 46) {
            if (baseTime.equals(eventTime.substring(0, 12) + "45")) {
                updateStats(value, max, min);
                saveStats(count);
            } else {
                updateStats(value, max, min);
                setBaseTime(eventTime, "45");
                saveStats(count);
                resetStats();
            }
        }

        // 45 ~ 00초 통계 - baseTime이 00초와 동일하면 변수 업데이트 & DB 저장
        else if (eventSecInt >= 46 || eventSecInt == 0) {
            if (baseTime.equals(eventTime.substring(0, 12) + "00")) {
                updateStats(value, max, min);
                saveStats(count);
            } else {
                updateStats(value, max, min);
                setBaseTime(eventTime, "00");
                saveStats(count);
                resetStats();
            }
        }
    }

    private void setBaseTime(String eventTime, String second) {
        baseTime = eventTime.substring(0,12) + second;
    }
}
