package statistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import statistics.entity.Svc30SecPk;
import statistics.entity.Svc30SecStat;

public interface Svc30SecStatRepository extends JpaRepository<Svc30SecStat, Svc30SecPk>, Svc30SecStatRepositoryCustom {
}
