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
@Table(name = "svc_1min_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("서비스-1분 통계")
public class Svc1MinStat {
    @EmbeddedId
    private Svc1MinPk id;

    @Column(name = "average_count", nullable = false, precision = 6, scale = 3)
    private BigDecimal averageCount;

    @Column(name = "min_people_count", nullable = false, precision = 6, scale = 3)
    private BigDecimal minPeopleCount;

    @Column(name = "max_people_count", nullable = false, precision = 6, scale = 3)
    private BigDecimal maxPeopleCount;

    @Builder
    public Svc1MinStat(Svc1MinPk id, BigDecimal averageCount, BigDecimal minPeopleCount, BigDecimal maxPeopleCount) {
        this.id = id;
        this.averageCount = averageCount;
        this.minPeopleCount = minPeopleCount;
        this.maxPeopleCount = maxPeopleCount;
    }
}