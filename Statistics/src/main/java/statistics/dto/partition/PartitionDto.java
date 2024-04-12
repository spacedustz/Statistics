package statistics.dto.partition;

import lombok.Data;

@Data
public class PartitionDto {
    public String tableName;
    public String partitionName;

    public PartitionDto(Object[] data) {
        this.tableName = (String) data[0];
        this.partitionName = (String) data[1];
    }
}
