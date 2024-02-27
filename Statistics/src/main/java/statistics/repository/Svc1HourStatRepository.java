package statistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import statistics.entity.Svc1HourPk;
import statistics.entity.Svc1HourStat;

public interface Svc1HourStatRepository extends JpaRepository<Svc1HourStat, Svc1HourPk> {
}
