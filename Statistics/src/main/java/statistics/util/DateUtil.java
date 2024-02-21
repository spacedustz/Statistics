package statistics.util;

import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class DateUtil {
    private static AtomicInteger TRAN_SEQ_NO = new AtomicInteger(0);

    public static String getTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(calendar.getTime());
    }

    public static String timestampToDate(final long timestamp, final String zone) {
        LocalDateTime localDateTime = null;

        if (!StringUtils.hasText(zone)) {
            localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp),
                    ZoneId.systemDefault());

        } else {
            localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of(zone));
        }

        // LocalDateTime 객체를 원하는 포맷으로 문자열로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedDate = localDateTime.format(formatter);

        return formattedDate;
    }
}
