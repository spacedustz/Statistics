package statistics.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import statistics.entity.Svc15SecPk;
import statistics.entity.Svc15SecStat;
import statistics.repository.*;
import statistics.service.redis.RedisService;
import statistics.thread.StatisticsThread;
import statistics.util.DateUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StatisticsService {
    private final Svc15SecStatRepository svc15SecStatRepository;
    private final Svc30SecStatRepository svc30SecStatRepository;
    private final Svc1MinStatRepository svc1MinStatRepository;
    private final Svc5MinStatRepository svc5MinStatRepository;
    private final Svc10MinStatRepository svc10MinStatRepository;
    private final Svc1HourStatRepository svc1HourStatRepository;
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

    /* 15초 통계 생성 */
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

    /* 30초 통계 생성, 15, 45초마다 실행 */
    @Scheduled(cron = "15,45 * * * * *")
    public void calculate30SecStats() {
        Date date = new Date();
        String now = DateUtil.getDate(date, "yyyyMMddHHmmss");
        int currentSec = Integer.parseInt(now.substring(12, 14));

        // 현재 시간의 초가 15 ~ 45초 사이이면 이전 분의 45, 00초 통계를 이용해 30초 통계 생성
        if (currentSec >= 15 && currentSec < 45) {
            String targetStatsDate = DateUtil.getDate(DateUtil.addMinutesToJavaDate(date, -1), "yyyyMMddHHmmss");
            String targetYyyyMmDd = targetStatsDate.substring(0, 8);
            String targetHhMm = targetStatsDate.substring(8, 12);

            svc30SecStatRepository.create30SecStats(targetYyyyMmDd, targetHhMm + "45", now.substring(8, 12) + "00");
        }
        // 현재 분의 15초, 30초 통계를 이용한 30초 통계 생성
        else {
            String yyyymmdd = now.substring(0, 8);
            String hhmm = now.substring(8, 12);

            svc30SecStatRepository.create30SecStats(yyyymmdd, hhmm + "15", hhmm + "30");
        }
    }

    /**
     * 1분 통계, n분 5초마다 실행
     * 15초 통계의 15,30,45, 다음 분의 00초를 이용해 1분 통계 저장
     */
    @Scheduled(cron = "5 * * * * *")
    public void calculate1MinStats() {
        Date date = new Date();
        String now = DateUtil.getDate(date, "yyyyMMddHHmmss");
        String targetStatsDate = DateUtil.getDate(DateUtil.addMinutesToJavaDate(date, -1), "yyyyMMddHHmmss");

        String yyyymmdd = targetStatsDate.substring(0, 8);
        String hhmm = targetStatsDate.substring(8, 12);

        svc1MinStatRepository.create1MinStats(yyyymmdd, hhmm + "15", now.substring(8, 12) + "00");
    }
}
