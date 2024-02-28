package statistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import statistics.entity.Svc1MinPk;
import statistics.entity.Svc1MinStat;

public interface Svc1MinStatRepository extends JpaRepository<Svc1MinStat, Svc1MinPk>, Svc1MinStatRepositoryCustom {
}
