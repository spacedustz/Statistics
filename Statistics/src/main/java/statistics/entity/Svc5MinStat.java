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
@Table(name = "svc_5sec_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("서비스-5분 통계")
public class Svc5MinStat {
    @EmbeddedId
    private Svc5MinPk id;

    @Column(name = "average_count", nullable = false, precision = 5, scale = 2)
    private BigDecimal averageCount;

    @Column(name = "min_people_count", nullable = false, precision = 5, scale = 2)
    private BigDecimal minPeopleCount;

    @Column(name = "max_people_count", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxPeopleCount;

    @Builder
    public Svc5MinStat(Svc5MinPk id, BigDecimal averageCount, BigDecimal minPeopleCount, BigDecimal maxPeopleCount) {
        this.id = id;
        this.averageCount = averageCount;
        this.minPeopleCount = minPeopleCount;
        this.maxPeopleCount = maxPeopleCount;
    }
}