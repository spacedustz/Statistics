package com.statistics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author 신건우
 * Cvedia의 Tripwire Event DTO
 */
@Getter
@Setter
@ToString
public class TripwireDto {
    private List<Event> events;
    private int frame_id;
    private double frame_time;
    private String system_date;
    private long system_timestamp;

    @Getter
    @Setter
    @ToString
    public static class Event {
        private Extra extra;
        private String id;
        private String label;
        private String type;
    }

    @Getter
    @Setter
    @ToString
    public static class Extra {
        private Bbox bbox;
        @JsonProperty("class")
        private String wireClass;
        private int count;
        private String crossing_direction;
        private String external_id;
        private String track_id;
        private Tripwire tripwire;
    }

    @Getter
    @Setter
    @ToString
    public static class Bbox {
        private double height;
        private double width;
        private double x;
        private double y;
    }

    @Getter
    @Setter
    @ToString
    public static class Tripwire {
        private String check_anchor_point;
        private List<Double> color;
        private double cooldown_bandwidth;
        private double cross_bandwidth;
        private int crowding_min_count;
        private boolean detect_animals;
        private boolean detect_people;
        private boolean detect_unknowns;
        private boolean detect_vehicles;
        private String direction;
        private String groupby;
        private String id;
        private boolean ignore_stationary_objects;
        private String inference_strategy;
        private String name;
        private boolean restrict_object_max_size;
        private boolean restrict_object_min_size;
        private boolean restrict_person_attributes;
        private boolean restrict_vehicle_type;
        private double timestamp;
        private boolean trigger_crossing;
        private boolean trigger_crowding;
        private boolean trigger_loitering;
        private boolean trigger_on_enter;
        private boolean trigger_on_exit;
        private List<Vertices> vertices;
    }

    @Getter
    @Setter
    @ToString
    public static class Vertices {
        private double x;
        private double y;
    }
}
