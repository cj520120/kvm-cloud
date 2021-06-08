package com.roamblue.cloud.management.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimeUtil {

    public static List<Date> getIntervalTimeList(Date startDate, Date endDate, int interval) {
        List<Date> list = new ArrayList<>();
        while (startDate.getTime() <= endDate.getTime()) {
            list.add(startDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.SECOND, interval);
            if (calendar.getTime().after(endDate)) {
                if (!startDate.equals(endDate)) {
                    list.add(endDate);
                }
            }
            startDate = calendar.getTime();

        }
        return list;
    }
}
