package statistics.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import statistics.entity.QSvc15SecStat;
import statistics.entity.QSvc30SecStat;

@Slf4j
@Repository
@RequiredArgsConstructor
public class Svc30SecStatRepositoryImpl implements Svc30SecStatRepositoryCustom {
    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    @Override
    public int create30SecStats(String yyyymmdd, String startHhmmss, String endHHmmss) {
        // 서브 쿼리 정의
        QSvc15SecStat source = QSvc15SecStat.svc15SecStat;
        QSvc30SecStat target = QSvc30SecStat.svc30SecStat;

        JPAQuery<Tuple> subQuery = queryFactory
                .select(
                        source.id.yyyymmdd,
                        source.id.hhmiss,
                        source.averageCount.avg().coalesce(0.0),
                        source.maxPeopleCount.max().castToNum(Double.class).coalesce(0.0),
                        source.minPeopleCount.min().castToNum(Double.class).coalesce(0.0),
                        source.id.instanceName
                )
                .from(source)
                .where(
                        source.id.yyyymmdd.eq(yyyymmdd),
                        source.id.hhmiss.between(startHhmmss, endHHmmss)
                )
                .groupBy(source.id.instanceName);

        return (int) queryFactory
                .insert(target)
                .columns(
                        target.id.yyyymmdd,
                        target.id.hhmiss,
                        target.averageCount,
                        target.maxPeopleCount,
                        target.minPeopleCount,
                        target.id.instanceName
                )
                .select(subQuery)
                .execute();

        /*
        // Native Query
        String query = new StringBuilder()
                .append("INSERT INTO svc_30sec_stats (yyyymmdd, hhmiss, average_count, max_people_count, min_people_count, instance_name)\n")
                .append("SELECT '").append(yyyymmdd).append("', '").append(endHHmmss)
                .append("', COALESCE(AVG(source.average_count), 0), COALESCE(MAX(source.max_people_count), 0), COALESCE(MIN(source.min_people_count), 0), source.instance_name \n")
                .append("FROM svc_15sec_stats as source ")
                .append("WHERE source.yyyymmdd = '").append(yyyymmdd)
                .append("' AND source.hhmiss BETWEEN '").append(startHhmmss).append("' AND '")
                .append(endHHmmss).append("' GROUP BY source.instance_name").toString();

        return em.createNativeQuery(query).executeUpdate();

         */
    }
}
