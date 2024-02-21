package statistics.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class DateUtil {
    private static AtomicInteger TRAN_SEQ_NO = new AtomicInteger(0);

    public static String getTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(calendar.getTime());
    }
}
