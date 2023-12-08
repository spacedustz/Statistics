package com.statistics.util;

import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
public class DateUtil {
    private static AtomicInteger TRAN_SEQ_NO = new AtomicInteger(0);

    public static String getTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(calendar.getTime());
    }
}
