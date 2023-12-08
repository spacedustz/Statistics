package com.statistics.service;

import com.statistics.constans.RedisConstants;
import com.statistics.entity.Count;
import com.statistics.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 신건우
 * 통계 생성용 Redis 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final RedisTemplate redisTemplate;

    public void save(Count count, String eventTime, String routingKey) {
        redisTemplate.opsForHash().put(
                RedisConstants.REDIS_KEY_TRIPWIRE_STATS + routingKey,
                eventTime,
                count.getCount());
    }

    /* camera:stats 로 시작하는 모든 키 조회 */
    public Set<String> getAllStatsKeys() {
        return redisTemplate.keys(RedisConstants.REDIS_KEY_CAMERA_STATS + "*");
    }

    /* 특정 키의 Hash Key, Value 삭제 */
    public long deleteHashKeys(String key, Object... hashKeys) {
        log.info("[Statistics 15sec] - Delete Hash Key : {}", key);

        for (Object hashKey : hashKeys) {
            log.info("[Statistics 15sec] - Hash Key : {}", hashKey);
        }

        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /* 특정 Key의 모든 Hash Key, Value 조화 */
    public Map<String, Integer> getAllHashKeyAndValues(String key) {
        Map<String, Integer> entries = redisTemplate.opsForHash().entries(key);

        Map<String, Integer> sortedEntries = entries.entrySet().stream()
                .sorted(Comparator.comparing(e -> Long.parseLong(e.getKey())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));

        return sortedEntries;
    }

    @Scheduled(cron = "3,18,33,48 * * * * *")
    public void calculate15secStatistics() {
        String curruntTime = DateUtil.getTime();
        log.info("[Statistics 15sec] - Start : {}", curruntTime);

        // TODO 1 : camera:stats: 로 시작하는 모든 키 조회
        Set<String> statsKeys = getAllStatsKeys();

        // TODO 2 : 현재 시간 조회
        String statsBaseTime = "";
        String eventTime = "";
        String eventSec = "";
        int eventSecInt = 0;
        int cameraId = 0;
        log.info("[Statistics 15sec] - CameraStats Size : {}", statsKeys != null ? statsKeys.size() : 0);

        for (String key : statsKeys) {
            log.info("[Statistics 15sec] - Key : {}", key);

            cameraId = Integer.parseInt(key.substring(RedisConstants.REDIS_KEY_CAMERA_STATS.length()));

            // camera:stats:{camera_id}를 Key로 하는 Hash Key,Value 조회
            Map<String, Integer> hashValues = getAllHashKeyAndValues(key);
            int sum = 0;
            int count = 0;

            Set<String> hashKeysToDelete = new HashSet<>();
            List<Map.Entry<String, Integer>> entryList = new ArrayList<>(hashValues.entrySet());
            int entrySize = entryList.size();
            log.info("[Statistics 15sec] - Entry Size : {}", entrySize);

            for (int i=0; i<entrySize; i++) {
                Map.Entry<String, Integer> entry = entryList.get(i);
                eventTime = entry.getKey();
                eventSec = eventTime.substring(12, 14);
                eventSecInt = Integer.parseInt(eventSec);
                log.info("[Statistics 15sec] - Event Time : {}, Event Sec : {}", eventTime, eventSecInt);

                // 첫 데이터인 경우와 데이터가 1개가 아닌 경우
                if (i == 0 && entrySize != 1) {
                    // 00 ~ 14초
                    if (eventSecInt >= 00 && eventSecInt < 15) {
                        statsBaseTime = eventTime.substring(0, 12) + "00";
                    }
                    // 15 ~ 29 초
                    else if(eventSecInt >=15 && eventSecInt < 30){
                        statsBaseTime = eventTime.substring(0,12) + "15";
                    }
                    // 30 ~ 44 초
                    else if(eventSecInt >=30 && eventSecInt < 45){
                        statsBaseTime = eventTime.substring(0,12) + "30";
                    }
                    // 45 ~ 59 초
                    else if(eventSecInt >=45 && eventSecInt <= 59){
                        statsBaseTime = eventTime.substring(0,12) + "45";
                    }
                }
                log.info("[Statistics 15sec] - Stats");


            }
        }
    }
}
