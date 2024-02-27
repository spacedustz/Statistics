package statistics.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class Svc30SecStatRepositoryImpl implements Svc30SecStatRepositoryCustom {
    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    @Override
    public int create30SecStats(String yyyymmdd, String startHhmmss, String endHHmmss) {
//        // 서브쿼리 정의
//        JPAQuery<Tuple> subQuery = queryFactory
//                .select(
//                        Expressions.constant("20231101"), // 복합키 필드 1
//                        Expressions.constant("000015"),   // 복합키 필드 2
//                        svcCamera.cameraId, // 복합키 필드 3
//                        svcCamera15SecStats.peopleCount.avg().coalesce(0.0),
//                        svcCamera15SecStats.maxPeopleCount.max().coalesce(new BigDecimal(0)),
//                        svcCamera15SecStats.minPeopleCount.min().coalesce(new BigDecimal(0)),
//                        Expressions.constant(0),
//                        Expressions.constant(0)
//                )
//                .from(svcCamera)
//                .leftJoin(svcCamera15SecStats)
//                .on(
//                        svcCamera.cameraId.eq(svcCamera15SecStats.id.cameraId)
//                )
//                .where(
//                        svcCamera.dataStatus.eq(DataStatus.ENABLE),
//                        svcCamera15SecStats.id.yyyymmdd.eq("20231101"),
//                        svcCamera15SecStats.id.hhmiss.between("000000", "000030"))
//                .groupBy(svcCamera.cameraId);
//
//        // 최종 삽입 쿼리
//        return queryFactory
//                .insert(svcCamera30SecStats)
//                .columns(
//                        svcCamera30SecStats.id.yyyymmdd,
//                        svcCamera30SecStats.id.hhmiss,
//                        svcCamera30SecStats.id.cameraId,
//                        svcCamera30SecStats.peopleCount,
//                        svcCamera30SecStats.maxPeopleCount,
//                        svcCamera30SecStats.minPeopleCount,
//                        svcCamera30SecStats.regId,
//                        svcCamera30SecStats.updId
//                )
//                .select(subQuery).execute();

        String query = new StringBuilder()
                .append("INSERT INTO svc_30sec_stats (yyyymmdd, hhmiss, average_count, max_people_count, min_people_count)\n")
                .append("SELECT '").append(yyyymmdd).append("', '").append(endHHmmss)
                .append("', COALESCE(AVG(source.average_count), 0), COALESCE(MAX(source.max_people_count), 0), COALESCE(MIN(source.min_people_count), 0) \n")
                .append("FROM svc_15sec_stats as source ")
                .append("WHERE source.yyyymmdd = '").append(startHhmmss)
                .append("' AND '").append(endHHmmss).append("' GROUP BY source.instance_name").toString();

        return em.createNativeQuery(query).executeUpdate();
    }
}
