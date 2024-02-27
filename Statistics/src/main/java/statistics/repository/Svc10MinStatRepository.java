package statistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import statistics.entity.Svc10MinPk;
import statistics.entity.Svc10MinStat;

public interface Svc10MinStatRepository extends JpaRepository<Svc10MinStat, Svc10MinPk> {
}
