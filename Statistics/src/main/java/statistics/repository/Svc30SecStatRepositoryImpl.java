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

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class Svc30SecStatRepositoryImpl implements Svc30SecStatRepositoryCustom {
    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    @Override
    public int create30SecStats(String yyyymmdd, String startHhmmss, String endHHmmss) {
//        // 서브 쿼리 정의
//        QSvc15SecStat source = QSvc15SecStat.svc15SecStat;
//        QSvc30SecStat target = QSvc30SecStat.svc30SecStat;
//
//        JPAQuery<Tuple> subQuery = queryFactory
//                .select(
//                        Expressions.stringTemplate(yyyymmdd),
//                        Expressions.stringTemplate(endHHmmss),
//                        source.averageCount.avg().castToNum(Double.class).coalesce(0.0),
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
//        log.info("\uD83D\uDCC4 30초 통계 저장 완료");
//
//        return result;
//        // 기본 키 존재 확인
//        long countTarger = queryFactory.selectFrom(target).fetchCount();
//        long count = 0;
//        if (countTarger != 0) {
//            count = queryFactory
//                    .selectFrom(target)
//                    .where(target.id.yyyymmdd.eq(yyyymmdd),
//                            target.id.hhmiss.between(startHhmmss, endHHmmss),
//                            target.id.instanceName.eq(source.id.instanceName))
//                    .fetchCount();
//        }
//
//        // 이미 복합키가 존재하면 기존 레코드 값 업데이트
//        if (count > 0) {
//            result = (int) queryFactory
//                    .update(target)
//                    .set(target.averageCount, source.averageCount.avg().coalesce(0.0).castToNum(BigDecimal.class))
//                    .set(target.maxPeopleCount, source.maxPeopleCount.max().castToNum(Double.class).coalesce(0.0).castToNum(BigDecimal.class))
//                    .set(target.minPeopleCount, source.minPeopleCount.min().castToNum(Double.class).coalesce(0.0).castToNum(BigDecimal.class))
//                    .where(target.id.yyyymmdd.eq(yyyymmdd), target.id.hhmiss.between(startHhmmss, endHHmmss), target.id.instanceName.eq(source.id.instanceName))
//                    .execute();
//        } else {
//            int result = (int) queryFactory
//                    .insert(target)
//                    .columns(
//                            target.id.yyyymmdd,
//                            target.id.hhmiss,
//                            target.averageCount,
//                            target.maxPeopleCount,
//                            target.minPeopleCount,
//                            target.id.instanceName
//                    )
//                    .select(subQuery)
//                    .execute();
//        }

        // Native Query
        String selectQuery = new StringBuilder()
                .append("SELECT ")
                .append("'").append(yyyymmdd).append("', ")
                .append("'").append(endHHmmss).append("', ")
                .append("COALESCE(ROUND(AVG(source.average_count), 3), 0), ")
                .append("COALESCE(ROUND(MAX(source.max_people_count), 3), 0), ")
                .append("COALESCE(ROUND(MIN(source.min_people_count), 3), 0), ")
                .append("source.instance_name\n")
                .append("FROM svc_15sec_stats as source\n")
                .append("WHERE source.yyyymmdd = '").append(yyyymmdd).append("'\n")
                .append("AND source.hhmiss BETWEEN '").append(startHhmmss).append("' AND '").append(endHHmmss).append("'\n")
                .append("GROUP BY source.instance_name")
                .toString();

        String selectMinPeopleCount = new StringBuilder()
                .append("SELECT source.min_people_count ")
                .append("FROM svc_15sec_stats as source\n")
                .append("WHERE source.yyyymmdd = '").append(yyyymmdd).append("'\n")
                .append("AND source.hhmiss BETWEEN '").append(startHhmmss).append("' AND '").append(endHHmmss).append("'\n")
                .toString();

        List<Object> selectResult = em.createNativeQuery(selectMinPeopleCount).getResultList();

        for (Object row : selectResult) {
            log.info("30초 통계 min_people_count : {}", row.toString());
        }

        String insertQuery = new StringBuilder()
                .append("INSERT INTO svc_30sec_stats ")
                .append("(yyyymmdd, hhmiss, average_count, max_people_count, min_people_count, instance_name)\n")
                .append(selectQuery)
                .toString();

        int result = 0;

        try {
            result = em.createNativeQuery(insertQuery).executeUpdate();
            if (result > 0) {
                log.info("\uD83D\uDCC4 30초 통계 저장 완료");
            }
        } catch (Exception e) {
            log.error("30초 통계 저장 실패 - {}", e.getMessage());
        }

        return result;
    }
}
