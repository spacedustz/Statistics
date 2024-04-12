package statistics.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 파티션 기간 타입
 */
public enum PeriodType {
    Daily("D"),
    Weekly("W"),
    Monthly("M"),
    Yearly("Y"),;

    private final String type;

    PeriodType(final String newType) {
        type = newType;
    }

    public String toStr() {
        return type;
    }

    private static final Map<String, PeriodType> lookup = new HashMap<>();

    static {
        for (PeriodType rt : PeriodType.values()) {
            lookup.put(rt.type, rt);
        }
    }

    public static PeriodType get(String type) {
        return lookup.get(type);
    }
}
