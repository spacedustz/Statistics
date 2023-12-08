package com.statistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AreaCrowdDto {
    @JsonProperty("events")
    private List<Event> events;

    @JsonProperty("frame_id")
    private int frameId;

    @JsonProperty("frame_time")
    private double frameTime;

    @JsonProperty("system_date")
    private String systemDate;

    @JsonProperty("system_timestamp")
    private long systemTimestamp;


    @Getter @Setter
    public static class Event {

        @JsonProperty("extra")
        private Extra extra;

        @JsonProperty("id")
        private String id;

        @JsonProperty("label")
        private String label;

        @JsonProperty("type")
        private String type;
    }

    @Getter @Setter
    public static class Extra {

        @JsonProperty("current_entries")
        private int currentEntries;

        @JsonProperty("total_hits")
        private int totalHits;
    }
}
