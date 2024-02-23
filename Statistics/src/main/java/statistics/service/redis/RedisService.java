package statistics.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import statistics.constants.RedisConstants;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public String getCurrentAlarmLevel(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void setCurrentAlarmLevel(String key, String alarmLevel) {
        redisTemplate.opsForValue().set(key, alarmLevel);
    }

    public void setInstanceEventTime(String key, String eventTime) {
        redisTemplate.opsForValue().set(key, eventTime);
    }

    public void setPeopleCount(String key, String eventTime, int peopleCount) {
        redisTemplate.opsForHash().put(key, eventTime, peopleCount);
    }

    public Set<String> getHashKeys(String key) {
        return redisTemplate.opsForHash().keys(key).stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    public Set<String> getAllStatsKeys() {
        return redisTemplate.keys(RedisConstants.INSTANCE + "*");
    }

    public void deleteHashKey(String key, String hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }

    public long deleteHashKeys(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    public String getHashValue(String key, String hashKey) {
        return (String) redisTemplate.opsForHash().get(key, hashKey);
    }

    public void setHashValue(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Map<String, Integer> getAllHashKeyAndValues(String key) {
        return redisTemplate.opsForHash().entries(key).entrySet().stream()
                .sorted(Comparator.comparing(e -> Long.parseLong(String.valueOf(e.getKey()))))
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> Integer.valueOf(String.valueOf(e.getValue())),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
