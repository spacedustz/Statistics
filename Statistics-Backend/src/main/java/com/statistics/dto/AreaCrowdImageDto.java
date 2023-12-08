package com.statistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AreaCrowdImageDto {
    @JsonProperty("events")
    private List<Event> events;
    @JsonProperty("frame_id")
    private Integer frameId;
    @JsonProperty("frame_time")
    private Double frameTime;
    @JsonProperty("system_date")
    private String systemDate;
    @JsonProperty("system_timestamp")
    private Long systemTimestamp;

    @Data
    public static class Event {

        @JsonProperty("date")
        private String date;

        @JsonProperty("extra")
        private Extra extra;

        @JsonProperty("frame_id")
        private int frameId;

        @JsonProperty("frame_time")
        private double frameTime;

        @JsonProperty("id")
        private String id;

        @JsonProperty("image")
        private String image;

        @JsonProperty("instance_id")
        private String instanceId;

        @JsonProperty("label")
        private String label;

        @JsonProperty("system_date")
        private String systemDate;

        @JsonProperty("tracks")
        private List<Track> tracks;
    }

    @Data
    public static class Extra {

        @JsonProperty("current_entries")
        private int currentEntries;

        @JsonProperty("total_hits")
        private int totalHits;

    }

    @Data
    public static class Track {

        @JsonProperty("bbox")
        private Bbox bbox;

        @JsonProperty("best_thumbnail")
        private Thumbnail bestThumbnail;

        @JsonProperty("class_label")
        private String classLabel;

        @JsonProperty("external_id")
        private String externalId;

        @JsonProperty("id")
        private String id;

        @JsonProperty("last_seen")
        private int lastSeen;

        @JsonProperty("movement_direction")
        private MovementDirection movementDirection;

        @JsonProperty("source_tracker_track_id")
        private int sourceTrackerTrackId;

    }

    @Data
    public static class Bbox {

        @JsonProperty("height")
        private double height;

        @JsonProperty("width")
        private double width;

        @JsonProperty("x")
        private double x;

        @JsonProperty("y")
        private double y;

    }

    @Data
    public static class Thumbnail {

        @JsonProperty("confidence")
        private double confidence;

        @JsonProperty("image")
        private String image;

        @JsonProperty("position")
        private Position position;

        @JsonProperty("timestamp")
        private int timestamp;

    }

    @Data
    public static class Position {

        @JsonProperty("height")
        private double height;

        @JsonProperty("width")
        private double width;

        @JsonProperty("x")
        private double x;

        @JsonProperty("y")
        private double y;
    }

    @Data
    public static class MovementDirection {

        @JsonProperty("x")
        private double x;

        @JsonProperty("y")
        private double y;
    }
}
