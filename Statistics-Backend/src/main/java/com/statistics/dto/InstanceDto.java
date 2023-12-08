package com.statistics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstanceDto {
    @JsonProperty("instance_name")
    private String instanceName;

    @JsonProperty("solution")
    private String solution;

    @JsonProperty("solution_name")
    private String solutionName;

    @JsonProperty("solution_path")
    private String solutionPath;

    @JsonProperty("solution_version")
    private String solutionVersion;

    @JsonProperty("state")
    private int state;
}