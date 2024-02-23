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
    private String baseTime = "";
    private String routingKey = "";

    public StatisticsThread(List<String> keyList, RedisService redisService, StatisticsService statisticsService) {
        this.keyList = keyList;
        this.redisService = redisService;
        this.statisticsService = statisticsService;
    }

    @Override
    public void run() {
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.get(i);
            String eventTime = "", eventSec = "";
            int eventSecInt = 0, totalSquareMeter = squareMeter, sum = 0, count = 0, max = 0, min = 0, value = 0;
            Set<String> hashKeyToDelete = new HashSet<>();
            routingKey = key.substring(RedisConstants.INSTANCE_COUNT.length());

            try {
                // 특정 Hash의 모든 Hash Key, Hash Value를 가져와 TreeMap으로 변환 - 키 기준 정렬
                Map<String, Integer> hashValues = redisService.getAllHashKeyAndValues(key);
                if (hashValues.isEmpty()) return;

                TreeMap<String, Integer> sortedHash = new TreeMap<>(hashValues);
                if (sortedHash.isEmpty()) return;

                log.info("===== 1. 최상위 Hash Key/Value 검증 =====");
                log.info("Hash 값이 비어있는지 : {}", hashValues.isEmpty());

                resetStats(count, sum, max, min, value);
                log.info("변수 초기화");

                // 기준이 되는 시간 (60초)
                long validStatTime = Long.parseLong(DateUtil.getDateTime(DateUtil.addSeconds(new Date(), -60)));
                log.info("Base Time = {}", validStatTime);

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

                    log.info("=== 2. Sorted Hash Key/Value 검증 ===");
                    log.info("Key = {}", eventTime);
                    log.info("Value = {}", value);

                    // Hash Key(Event Time)이 유호시간(60초)보다 이전이거나 값이 0이면 건너뜀
                    if (Long.parseLong(eventTime) < validStatTime || value == 0) continue;

                    // 첫번쨰 Entry이고, Entry의 수가 1이 아니면 기준 시간 설정
                    if (x == 0 && entryList.size() != 1) {
                        baseTime = getBaseTime(eventSecInt, eventTime);

                        sum += value;
                        count++;
                        max = value;
                        min = value;

                        log.info("첫번쨰 Entry 데이터 처리");
                        // 마지막 데이터가 아닌경우
                    } else if (x + 1 != entryList.size()) {
                        processStatsByTimeRange(eventSecInt, baseTime, eventTime, value, count, sum, totalSquareMeter, max, min);
                        log.info("마지막 데이터가 아닌 경우 데이터 처리");
                        // 마지막 데이터인 경우
                    } else if (x + 1 == entryList.size()) {
                        processStatsByTimeRange2(eventSecInt, baseTime, eventTime, value, count, sum, totalSquareMeter, max, min);
                        log.info("마지막 Entry 데이터 처리");
                    }
                } // For Loop
            } catch (Exception e) {
                log.error("통계 Thread Exception - {}", e.getMessage());
                e.printStackTrace();
            } finally {
                // 평균 값을 구하는데 사용한 데이터를 Redis에서 제거
                if (!hashKeyToDelete.isEmpty()) {
                    redisService.deleteHashKeys(key, hashKeyToDelete.toArray());
                    log.info("통계 Thread - Success Delete Hash Keys");
                }
            }
        }
    }

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

    private void resetStats(int count, int sum, int max, int min, int value) {
        count = 0;
        sum = 0;
        max = 0;
        min = 0;
        value = 0;
    }

    private void updateStats(int value, int count, int sum, int max, int min) {
        if (max < value) max = value;
        if (min > value) min = value;
        sum += value;
        count++;
    }

    private void updateStats2(int value, int count, int sum) {
        sum += value;
        count++;
    }

    private void saveStats(int count, int sum, int totalSquareMeter, int max, int min) {
        if (count > 0) {
            // 평균값 계산
            BigDecimal average = BigDecimal.valueOf(sum)
                    .divide(BigDecimal.valueOf(count))
                    .divide(BigDecimal.valueOf(totalSquareMeter), 3, BigDecimal.ROUND_HALF_UP);

            // DB에 저장
            statisticsService.save15SecAvgToDB(
                    baseTime.substring(0, 8),
                    baseTime.substring(8, 14),
                    routingKey,
                    average,
                    new BigDecimal(max).divide(BigDecimal.valueOf(totalSquareMeter), 3, BigDecimal.ROUND_HALF_UP),
                    new BigDecimal(min).divide(BigDecimal.valueOf(totalSquareMeter), 3, BigDecimal.ROUND_HALF_UP)
            );

            log.info("통계 데이터 MariaDB 저장 완료");
        }
    }

    private void processStatsByTimeRange(int eventSecInt, String baseTime, String eventTime, int value, int count, int sum, int totalSquareMeter, int max, int min) {
        // 01 ~ 15초
        if (eventSecInt >= 1 && eventSecInt < 16) {
            if (baseTime.equals(eventTime.substring(0, 12) + "15")) {
                updateStats(value, count, sum, max, min);
            } else {
                saveStats(count, sum, totalSquareMeter, max, min);
                resetStats(count, sum, max, min, value);
                setBaseTime(eventTime, "15");
                updateStats(value, count, sum, max, min);
            }
        }
        // 16 ~ 30초
        else if (eventSecInt >= 16 && eventSecInt < 31) {
            if (baseTime.equals(eventTime.substring(0, 12) + "30")) {
                updateStats(value, count, sum, max, min);
            } else {
                saveStats(count, sum, totalSquareMeter, max, min);
                resetStats(count, sum, max, min, value);
                setBaseTime(eventTime, "30");
                updateStats(value, count, sum, max, min);
            }
        }

        // 31 ~ 45초
        else if (eventSecInt >= 31 && eventSecInt < 46) {
            if (baseTime.equals(eventTime.substring(0, 12) + "45")) {
                updateStats(value, count, sum, max, min);
            } else {
                saveStats(count, sum, totalSquareMeter, max, min);
                resetStats(count, sum, max, min, value);
                setBaseTime(eventTime, "45");
                updateStats(value, count, sum, max, min);
            }
        }

        // 16 ~ 30초
        else if (eventSecInt >= 46 || eventSecInt == 0) {
            if (baseTime.equals(eventTime.substring(0, 12) + "00")) {
                updateStats(value, count, sum, max, min);
            } else {
                saveStats(count, sum, totalSquareMeter, max, min);
                resetStats(count, sum, max, min, value);
                setBaseTime(eventTime, "00");
                updateStats(value, count, sum, max, min);
            }
        }
    }

    private void processStatsByTimeRange2(int eventSecInt, String baseTime, String eventTime, int value, int count, int sum, int totalSquareMeter, int max, int min) {
        // 01 ~ 15초
        if (eventSecInt >= 1 && eventSecInt < 16) {
            if (baseTime.equals(eventTime.substring(0, 12) + "15")) {
                updateStats2(value, count, sum);
                saveStats(count, sum, totalSquareMeter, max, min);
            } else {
                updateStats(value, count, sum, max, min);
                setBaseTime(eventTime, "15");
                saveStats(count, sum, totalSquareMeter, max, min);
                resetStats(count, sum, max, min, value);
            }
        }
        // 16 ~ 30초
        else if (eventSecInt >= 16 && eventSecInt < 31) {
            if (baseTime.equals(eventTime.substring(0, 12) + "30")) {
                updateStats(value, count, sum, max, min);
                saveStats(count, sum, totalSquareMeter, max, min);
            } else {
                updateStats(value, count, sum, max, min);
                setBaseTime(eventTime, "30");
                saveStats(count, sum, totalSquareMeter, max, min);
                resetStats(count, sum, max, min, value);
            }
        }

        // 31 ~ 45초
        else if (eventSecInt >= 31 && eventSecInt < 46) {
            if (baseTime.equals(eventTime.substring(0, 12) + "45")) {
                updateStats(value, count, sum, max, min);
                saveStats(count, sum, totalSquareMeter, max, min);
            } else {
                updateStats(value, count, sum, max, min);
                setBaseTime(eventTime, "45");
                saveStats(count, sum, totalSquareMeter, max, min);
                resetStats(count, sum, max, min, value);
            }
        }

        // 16 ~ 30초
        else if (eventSecInt >= 46 || eventSecInt == 0) {
            if (baseTime.equals(eventTime.substring(0, 12) + "00")) {
                updateStats(value, count, sum, max, min);
                saveStats(count, sum, totalSquareMeter, max, min);
            } else {
                updateStats(value, count, sum, max, min);
                setBaseTime(eventTime, "00");
                saveStats(count, sum, totalSquareMeter, max, min);
                resetStats(count, sum, max, min, value);
            }
        }
    }

    private void setBaseTime(String eventTime, String second) {
        baseTime = eventTime.substring(0,12) + second;
    }
}
