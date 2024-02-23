package statistics.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import statistics.entity.Svc15SecPk;
import statistics.entity.Svc15SecStat;
import statistics.repository.Svc15SecStatRepository;

import java.math.BigDecimal;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StatisticsService {
    private final Svc15SecStatRepository svc15SecStatRepository;

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
}
