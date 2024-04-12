package statistics.repository.partition;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import statistics.dto.partition.PartitionDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PartitionRepository {
    private final EntityManager em;

    @Transactional
    public int addPartition(final String tableName, final String partitionName, final String basseDate, final boolean isTimeStamp) {
        String query = "";

        if (isTimeStamp) {
            query = "ALTER TABLE " + tableName + " ADD PARTITION (PARTITION " + partitionName + " VALUES LESS THAN (" + basseDate + "))";
        } else {
            query = "ALTER TABLE " + tableName + " ADD PARTITION (PARTITION " + partitionName + " VALUES LESS THAN ('" + basseDate + "'))";
        }

        return em.createNativeQuery(query).executeUpdate();
    }

    @Transactional
    public int dropPartition(final String tableName, final String partitionName) {
        String query = "ALTER TABLE " + tableName + " DROP PARTITION " + partitionName;
        return em.createNativeQuery(query).executeUpdate();
    }

    public Optional<PartitionDto> findPartition(final String tableName, final String partitionName) {
        Query query = em.createNativeQuery("SELECT TABLE_NAME AS TABLE_NAME, PARTITION_NAME AS PARTITION_NAME FROM INFORMATION_SCHEMA.PARTITIONS WHERE TABLE_NAME = :tableName AND PARTITION_NAME = :partitionName");
        query.setParameter("tableName", tableName);
        query.setParameter("partitionName", partitionName);

        try {
            List<Object[]> results = query.getResultList();

            if (!results.isEmpty()) {
                PartitionDto result = new PartitionDto(results.get(0));
                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<List<PartitionDto>> findPartition(final String tableName, final String partitionName, final String addOrDrop) {
        Query query = null;
        List<PartitionDto> resultList = new ArrayList<>();

        if (addOrDrop.equalsIgnoreCase("A")) {
            query = em.createNativeQuery("SELECT TABLE_NAME AS TABLE_NAME, PARTITION_NAME AS PARTITION_NAME FROM INFORMATION_SCHEMA.PARTITIONS WHERE TABLE_NAME = :tableName AND PARTITION_NAME > :partitionName ORDER BY PARTITION_NAME");
        } else if (addOrDrop.equalsIgnoreCase("D")) {
            query = em.createNativeQuery("SELECT TABLE_NAME AS TABLE_NAME, PARTITION_NAME AS PARTITION_NAME FROM INFORMATION_SCHEMA.PARTITIONS WHERE TABLE_NAME = :tableName AND PARTITION_NAME < :partitionName ORDER BY PARTITION_NAME");
        }
        query.setParameter("tableName", tableName);
        query.setParameter("partitionName", partitionName);


        try {
            List<Object[]> results = query.getResultList();

            if (!results.isEmpty()) {
                results.forEach(result -> resultList.add(new PartitionDto(result)));
                return Optional.of(resultList);
            } else {
                return Optional.empty();
            }
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<ColumnDto>
}
