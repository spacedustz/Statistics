package statistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import statistics.entity.Svc15SecPk;
import statistics.entity.Svc15SecStat;

public interface Svc15SecStatRepository extends JpaRepository<Svc15SecStat, Svc15SecPk> {
}
