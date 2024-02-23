package statistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Svc15SecPk implements Serializable {
    @Serial
    private static final long serialVersionUID = 1035027059430034197L;

    @Column(name = "yyyymmdd", nullable = false, length = 8)
    @Comment("생성일자")
    private String yyyymmdd;


    @Column(name = "hhmiss", nullable = false, length = 6)
    @Comment("시간분초")
    private String hhmiss;

    @Column(name = "instance_name", nullable = false)
    @Comment("Instance Name")
    private String instanceName;

    @Builder
    public Svc15SecPk(String yyyymmdd, String hhmiss, String instanceName) {
        this.yyyymmdd = yyyymmdd;
        this.hhmiss = hhmiss;
        this.instanceName = instanceName;
    }
}