package statistics.util;

import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class DateUtil {
    private static AtomicInteger TRAN_SEQ_NO = new AtomicInteger(0);

    public static String getTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(calendar.getTime());
    }

    public static String getDate(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static String getTimeMilli() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
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

    public static long getSecondsDifference(String start, String end) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            Date startDate = format.parse(start);
            Date endDate = format.parse(end);

            long difference = endDate.getTime() - startDate.getTime();

            return difference / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getDateTime(Date date) {
        String result = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        result = dateFormat.format(date);
        return result;
    }

    // 입력받은 Date에 Seconds 추가
    public static Date addSeconds(Date date, Integer seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    public static Date addMinutesToJavaDate(Date date, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minute);

        return calendar.getTime();
    }
}
