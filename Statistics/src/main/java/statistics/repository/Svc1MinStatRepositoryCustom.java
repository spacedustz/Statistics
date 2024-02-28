package statistics.repository;

import jakarta.transaction.Transactional;

public interface Svc1MinStatRepositoryCustom {
    @Transactional
    int create1MinStats(final String yyyymmdd, final String startHhmmss, final String endHHmmss);
}
