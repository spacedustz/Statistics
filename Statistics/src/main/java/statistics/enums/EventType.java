package statistics.enums;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
    SecuRtAreaCrowdingImageEventDto("SecuRtAreaCrowdingImageEventDto"),
    SecuRtAreaCrowdingEventDto("SecuRtAreaCrowdingEventDto"),
    CrowdEstimationEventDto("CrowdEstimationEventDto"),
    SecuRTAreaOccupancyEnterEventDto("SecuRTAreaOccupancyEnterEventDto"),
    SecuRTAreaOccupancyExitEventDto("SecuRTAreaOccupancyExitEventDto"),

    SecuRTAreaOccupancyEnterEventImageDto("SecuRTAreaOccupancyEnterEventImageDto"),
    SecuRTAreaOccupancyExitEventImageDto("SecuRTAreaOccupancyExitEventImageDto"),
    SecuRtTripwireEventDto("SecuRtTripwireEventDto"),

    SecuRtTripwireEventImageDto("SecuRtTripwireEventImageDto"),

    UNKNOWN("UNKNOWN");

    private final String stringValue;

    private EventType(final String newValue) {
        stringValue = newValue;
    }

    public String toStr() {
        return stringValue;
    }

    private static final Map<String, EventType> lookup = new HashMap<String, EventType>();

    static {
        for (EventType rt : EventType.values()) {
            lookup.put(rt.stringValue, rt);
        }
    }

    public static EventType get(String typeStr) {
        return lookup.get(typeStr);
    }
}