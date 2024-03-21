package statistics.thread;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import statistics.constants.RedisConstants;
import statistics.dto.stats.Stats15SecDto;
import statistics.service.StatisticsService;
import statistics.service.redis.RedisService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class StatisticsThread extends Thread {
    private final Set<String> keyList;
    private final RedisService redisService;
    private final StatisticsService statisticsService;

    @Override
    public void run() {
        for (String key : keyList) {
            String routingKey = key.substring(RedisConstants.INSTANCE_COUNT.length());
            int totalSquareMeter = new Random().nextInt(21) + 10;

            try {
                // 특정 Hash의 모든 Hash Key, Hash Value를 가져와 TreeMap으로 변환 - 키 기준 정렬
                Map<String, Integer> hashValues = redisService.getAllHashKeyAndValues(key);
                if (hashValues.isEmpty()) return;

                TreeMap<String, Integer> sortedHash = new TreeMap<>(hashValues);
                if (sortedHash.isEmpty()) return;

                // 데이터 15초 통계 처리
                Stats15SecDto data = processHashData(sortedHash);

                // DB에 저장
                saveStats(data, routingKey, totalSquareMeter);
            } catch (Exception e) {
                log.error("Statistics Thread Exception - {}", e.getMessage());
            }
        }
    }

    private Stats15SecDto processHashData(TreeMap<String, Integer> sortedHash) {
        Stats15SecDto data = new Stats15SecDto();
        if (sortedHash != null && !sortedHash.isEmpty()) {
            List<Map.Entry<String, Integer>> entryList = new ArrayList<>(sortedHash.entrySet());
            int size = entryList.size();

            log.info("Stats Thread - Grouped Hash Entry Size : {}", size);

            for (Map.Entry<String, Integer> entry : entryList) {
                data.addEntry(entry);
            }
        }
        return data;
    }

    private void saveStats(Stats15SecDto data, String routingKey, int totalSquareMeter) {
        boolean isBaseTimeIs00 = isEmpty(data.getBaseTime00Sec());
        boolean isBaseTimeIs15 = isEmpty(data.getBaseTime15Sec());
        boolean isBaseTimeIs30 = isEmpty(data.getBaseTime30Sec());
        boolean isBaseTimeIs45 = isEmpty(data.getBaseTime45Sec());

        if (!isBaseTimeIs00) {
            String yyyymmdd = data.getBaseTime00Sec().substring(0,8);
            String hhmiss = data.getBaseTime00Sec().substring(8,14);

            BigDecimal avg = calculateAverage(data.getSum00(), data.getCount00(), totalSquareMeter);
            BigDecimal max = BigDecimal.valueOf(data.getMax00()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP);
            BigDecimal min = BigDecimal.valueOf(data.getMin00()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP);

            statisticsService.save15SecAvgToDB(yyyymmdd, hhmiss, routingKey, avg, max, min);
        }

        if (!isBaseTimeIs15) {
            String yyyymmdd = data.getBaseTime15Sec().substring(0,8);
            String hhmiss = data.getBaseTime15Sec().substring(8,14);

            BigDecimal avg = calculateAverage(data.getSum15(), data.getCount15(), totalSquareMeter);
            BigDecimal max = BigDecimal.valueOf(data.getMax15()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP);
            BigDecimal min = BigDecimal.valueOf(data.getMin15()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP);

            statisticsService.save15SecAvgToDB(yyyymmdd, hhmiss, routingKey, avg, max, min);
        }

        if (!isBaseTimeIs30) {
            String yyyymmdd = data.getBaseTime30Sec().substring(0,8);
            String hhmiss = data.getBaseTime30Sec().substring(8,14);

            BigDecimal avg = calculateAverage(data.getSum30(), data.getCount30(), totalSquareMeter);
            BigDecimal max = BigDecimal.valueOf(data.getMax30()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP);
            BigDecimal min = BigDecimal.valueOf(data.getMin30()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP);

            statisticsService.save15SecAvgToDB(yyyymmdd, hhmiss, routingKey, avg, max, min);
        }

        if (!isBaseTimeIs45) {
            String yyyymmdd = data.getBaseTime45Sec().substring(0,8);
            String hhmiss = data.getBaseTime45Sec().substring(8,14);

            BigDecimal avg = calculateAverage(data.getSum45(), data.getCount45(), totalSquareMeter);
            BigDecimal max = BigDecimal.valueOf(data.getMax45()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP);
            BigDecimal min = BigDecimal.valueOf(data.getMin45()).divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP);

            statisticsService.save15SecAvgToDB(yyyymmdd, hhmiss, routingKey, avg, max, min);
        }
    }

    private BigDecimal calculateAverage(int sum, int count, int totalSquareMeter) {
        return BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(count), 3, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(totalSquareMeter), 3, RoundingMode.HALF_UP);
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
