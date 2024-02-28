package statistics.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import statistics.entity.QSvc15SecStat;
import statistics.entity.QSvc1MinStat;

@Slf4j
@Repository
@RequiredArgsConstructor
public class Svc1MinStatRepositoryImpl implements Svc1MinStatRepositoryCustom {
    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    @Override
    public int create1MinStats(String yyyymmdd, String startHhmmss, String endHHmmss) {
//        // 서브 쿼리 정의
//        QSvc15SecStat source = QSvc15SecStat.svc15SecStat;
//        QSvc1MinStat target = QSvc1MinStat.svc1MinStat;
//
//        JPAQuery<Tuple> subQuery = queryFactory
//                .select(
//                        source.id.yyyymmdd,
//                        source.id.hhmiss,
//                        source.averageCount.avg().coalesce(0.0),
//                        source.maxPeopleCount.max().castToNum(Double.class).coalesce(0.0),
//                        source.minPeopleCount.min().castToNum(Double.class).coalesce(0.0),
//                        source.id.instanceName
//                )
//                .from(source)
//                .where(
//                        source.id.yyyymmdd.eq(yyyymmdd),
//                        source.id.hhmiss.between(startHhmmss, endHHmmss)
//                )
//                .groupBy(source.id.instanceName);
//
//        int result = (int) queryFactory
//                .insert(target)
//                .columns(
//                        target.id.yyyymmdd,
//                        target.id.hhmiss,
//                        target.averageCount,
//                        target.maxPeopleCount,
//                        target.minPeopleCount,
//                        target.id.instanceName
//                )
//                .select(subQuery)
//                .execute();
//
//        queryFactory
//                .update(target)
//                .set(target.id.hhmiss, startHhmmss.substring(0, 4))
//                .where(
//                        target.id.yyyymmdd.eq(yyyymmdd),
//                        target.id.instanceName.in(
//                                JPAExpressions.select(source.id.instanceName)
//                                        .from(source)
//                                        .where(source.id.yyyymmdd.eq(yyyymmdd), source.id.hhmiss.between(startHhmmss, endHHmmss))
//                        )
//                )
//                .execute();
//
//        log.info("\uD83D\uDCC4 1분 통계 저장 완료");
//
//        return result;

        // Native Query
        String query = new StringBuilder()
                .append("INSERT INTO svc_1min_stats ")
                .append("(yyyymmdd, hhmiss, average_count, max_people_count, min_people_count, instance_name)\n")
                .append("SELECT ")
                .append("'").append(yyyymmdd).append("', ")
                .append("'").append(startHhmmss.substring(0, 4)).append("', ")
                .append("COALESCE(ROUND(AVG(source.average_count), 2), 0), ")
                .append("COALESCE(ROUND(MAX(source.max_people_count), 2), 0), ")
                .append("COALESCE(ROUND(MIN(source.min_people_count), 2), 0), ")
                .append("source.instance_name\n")
                .append("FROM svc_15sec_stats as source\n")
                .append("WHERE source.yyyymmdd = '").append(yyyymmdd).append("'\n")
                .append("AND source.hhmiss BETWEEN '").append(startHhmmss).append("' AND '").append(endHHmmss).append("'\n")
                .append("GROUP BY source.instance_name")
                .toString();

        log.info("\uD83D\uDCC4 1분 통계 저장 완료");
        return em.createNativeQuery(query).executeUpdate();
    }
}
