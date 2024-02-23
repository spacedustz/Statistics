package statistics.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import statistics.entity.Svc15SecPk;
import statistics.entity.Svc15SecStat;
import statistics.repository.Svc15SecStatRepository;
import statistics.service.redis.RedisService;
import statistics.thread.StatisticsThread;
import statistics.util.DateUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StatisticsService {
    private final Svc15SecStatRepository svc15SecStatRepository;
    private final RedisService redisService;
    private final TaskExecutor executor;

    public void save15SecAvgToDB(final String yyyymmdd,
                                 final String hhmiss,
                                 final String instanceName,
                                 final BigDecimal average,
                                 final BigDecimal max,
                                 final BigDecimal min) {
        Svc15SecPk pk = Svc15SecPk.builder()
                .yyyymmdd(yyyymmdd)
                .hhmiss(hhmiss)
                .instanceName(instanceName)
                .build();

        Svc15SecStat stats = Svc15SecStat.builder()
                .id(pk)
                .averageCount(average)
                .maxPeopleCount(max)
                .minPeopleCount(min)
                .build();

        try {
            svc15SecStatRepository.save(stats);
        } catch (Exception e) {
            log.error("15초 통계 저장 실패 - {}", e.getMessage());
        }
    }

    @Scheduled(cron = "3,18,33,48 * * * * *")
    public void calculate15SecStats() {
        List<String> keyList = new ArrayList<>();
        String currentTime = DateUtil.getTime();
        Set<String> statsKeys = redisService.getAllStatsKeys();

        for (String key : statsKeys) {
            keyList.add(key);

            if (keyList.size() >= 15) {
                List<String> keyListCopy = new ArrayList<>(keyList);
                StatisticsThread thread = new StatisticsThread(keyListCopy, redisService, this);

                executor.execute(thread);
                keyList.clear();
            }
        }

        if (!keyList.isEmpty()) {
            StatisticsThread thread = new StatisticsThread(keyList, redisService, this);
            executor.execute(thread);
        }
    }
}
