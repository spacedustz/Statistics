package statistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import statistics.entity.Svc5MinPk;
import statistics.entity.Svc5MinStat;

public interface Svc5MinStatRepository extends JpaRepository<Svc5MinStat, Svc5MinPk> {
}
