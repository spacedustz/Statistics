package statistics.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * CVEDIA 에서 SecuRT Model 로 Instance 를 실행하였을 때 감시 영역에 Exit 이벤트가 발생할 시 전달되는 이벤트의 JSON 규격(이미지 포함)
 *
 * @author: spacedustz
 * @version: 1.0
 * @since: 2023/10/13
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecuRTAreaOccupancyExitEventImageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -5914174688010286952L;
    @JsonProperty("events")
    private List<Event> events;

    @JsonProperty("frame_id")
    private int frameId;

    @JsonProperty("frame_time")
    private double frameTime;

    @JsonProperty("system_date")
    private String systemDate;

    @JsonProperty("system_timestamp")
    private String systemTimestamp;

    private String instanceName;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {
        @JsonProperty("best_thumbnail")
        private Thumbnail bestThumbnail;

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

        @JsonProperty("subtype")
        private String subtype;

        @JsonProperty("system_date")
        private String systemDate;

        @JsonProperty("tracks")
        private List<Track> tracks;

        @JsonProperty("type")
        private String type;

        @JsonProperty("zone_id")
        private String zoneId;

        @JsonProperty("zone_name")
        private String zoneName;

        // getters and setters...
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Thumbnail {
        @JsonProperty("confidence")
        private double confidence;

        @JsonProperty("image")
        private String image;

        @JsonProperty("position")
        private Position position;

        @JsonProperty("timestamp")
        private double timestamp;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Extra {
        @JsonProperty("bbox")
        private Bbox bbox;

        @JsonProperty("class")
        private String classLabel;

        @JsonProperty("current_entries")
        private int currentEntries;

        @JsonProperty("external_id")
        private String externalId;

        @JsonProperty("external_track_id_left")
        private String externalTrackIdLeft;

        @JsonProperty("total_hits")
        private int totalHits;

        @JsonProperty("track_id_left")
        private String trackIdLeft;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MovementDirection {
        @JsonProperty("x")
        private double x;

        @JsonProperty("y")
        private double y;
    }
}

