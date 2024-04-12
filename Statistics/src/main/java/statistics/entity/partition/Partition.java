package statistics.entity.partition;

import com.sun.istack.NotNull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.annotations.Comment;
import statistics.enums.PeriodType;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Partition {
    @Id
    @Column(length = 100, nullable = false)
    @Comment("파티션 테이블명")
    private String tableName;

    @NotNull
    @Column(nullable = false, length = 1)
    @Comment("파티션 기간타입 (D/W/M/Y)")
    private PeriodType periodType;

    @NotNull
    @Column(length = 1, nullable = false)
    @Comment("파티션 보유 기간")
    private Integer retentionPeriod;

    @NotNull
    @Column(nullable = false)
    @Comment("파티션 사전 생성 기간 (period_type 값에 따라 일/주/월/년수 & period_type이 D 이고 pre_creation_period 가 30이면 30일 사전 생성)")
    private Integer preCreationPeriod;

    @NotEmpty
    @Comment("파티션 접두어명")
    @Column(length = 20, nullable = false)
    private String prefixName;

    @NotEmpty
    @Column(length = 30, nullable = false)
    @Comment("파티션 컬럼명")
    private String columnName;

    @Builder
    public Partition(String tableName, PeriodType periodType, Integer retentionPeriod, Integer preCreationPeriod, String prefixName, String columnName) {
        this.tableName = tableName;
        this.periodType = periodType;
        this.retentionPeriod = retentionPeriod;
        this.preCreationPeriod = preCreationPeriod;
        this.prefixName = prefixName;
        this.columnName = columnName;
    }
}
