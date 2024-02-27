package statistics.repository;

import org.springframework.transaction.annotation.Transactional;

public interface Svc30SecStatRepositoryCustom {
    @Transactional
    int create30SecStats(final String yyyymmdd, final String startHhmmss, final String endHHmmss);
}
