package com.statistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EstimationDto {
    @JsonProperty("count")
    private int count;

    @JsonProperty("frame_id")
    private int frameId;

    @JsonProperty("frame_time")
    private double frameTime;

    @JsonProperty("instance_id")
    private String instanceId;

    @JsonProperty("system_date")
    private String systemDate;

    @JsonProperty("system_timestamp")
    private long systemTimestamp;
}
