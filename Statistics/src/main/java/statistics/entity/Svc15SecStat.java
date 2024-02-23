package statistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Table(name = "svc_15sec_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("서비스-15초 통계")
public class Svc15SecStat {
    @EmbeddedId
    private Svc15SecPk id;

    @Column(name = "average_count", nullable = false, precision = 5, scale = 2)
    private BigDecimal averageCount;

    @Column(name = "min_people_count", nullable = false, precision = 5, scale = 2)
    private BigDecimal minPeopleCount;

    @Column(name = "max_people_count", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxPeopleCount;

    @Builder
    public Svc15SecStat(Svc15SecPk id, Integer regId, Integer updId,
                               BigDecimal averageCount, BigDecimal minPeopleCount, BigDecimal maxPeopleCount) {
        this.id = id;
        this.averageCount = averageCount;
        this.minPeopleCount = minPeopleCount;
        this.maxPeopleCount = maxPeopleCount;
    }
}