package com.wm.remusic.request;

import java.util.Calendar;

public class TimeUtil {
    public static final String DOT = ".";
    public static final String COLON = ":";
    public static final String STRIKE = "-";
    public static final String UNDERLINE = "_";

    public static Calendar sCalendar;

    static {
        sCalendar = Calendar.getInstance();
    }


    public synchronized static int getYear(long millisecond) {
        long time = sCalendar.getTimeInMillis();
        if (time != millisecond) {
            sCalendar.setTimeInMillis(millisecond);
        }
        return sCalendar.get(Calendar.YEAR);
    }

    public synchronized static int getMonth(long millisecond) {
        long time = sCalendar.getTimeInMillis();
        if (time != millisecond) {
            sCalendar.setTimeInMillis(millisecond);
        }
        int month = sCalendar.get(Calendar.MONTH) + 1;
        return month;
    }

    public synchronized static String getMonth(long millisecond, boolean append) {
        int month = getMonth(millisecond);
        String strMonth;
        if (append && month < 10) {
            strMonth = "0" + Integer.toString(month);
        } else {
            strMonth = Integer.toString(month);
        }
        return strMonth;
    }

    public synchronized static int getDay(long millisecond) {
        long time = sCalendar.getTimeInMillis();
        if (time != millisecond) {
            sCalendar.setTimeInMillis(millisecond);
        }
        int day = sCalendar.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    public synchronized static String getDay(long millisecond, boolean append) {
        int day = getDay(millisecond);
        String strDay;
        if (append && day < 10) {
            strDay = "0" + Integer.toString(day);
        } else {
            strDay = Integer.toString(day);
        }
        return strDay;
    }

    public synchronized static int getHour(long millisecond) {
        long time = sCalendar.getTimeInMillis();
        if (time != millisecond) {
            sCalendar.setTimeInMillis(millisecond);
        }
        int hour = sCalendar.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    public static String getHour(long millisecond, boolean append) {
        int hour = getHour(millisecond);
        String strHour;
        if (append && hour < 10) {
            strHour = "0" + Integer.toString(hour);
        } else {
            strHour = Integer.toString(hour);
        }
        return strHour;
    }


    public synchronized static int getMinute(long millisecond) {
        long time = sCalendar.getTimeInMillis();
        if (time != millisecond) {
            sCalendar.setTimeInMillis(millisecond);
        }
        int minute = sCalendar.get(Calendar.MINUTE);
        return minute;
    }

    public static String getMinute(long millisecond, boolean append) {
        int minute = getMinute(millisecond);
        String strMinute;
        if (append && minute < 10) {
            strMinute = "0" + Integer.toString(minute);
        } else {
            strMinute = Integer.toString(minute);
        }
        return strMinute;
    }

    public synchronized static int getSecond(long millisecond) {
        long time = sCalendar.getTimeInMillis();
        if (time != millisecond) {
            sCalendar.setTimeInMillis(millisecond);
        }
        int second = sCalendar.get(Calendar.SECOND);
        return second;
    }

    public static String getSecond(long millisecond, boolean append) {
        int second = getSecond(millisecond);
        String strSecond;
        if (append && second < 10) {
            strSecond = "0" + Integer.toString(second);
        } else {
            strSecond = Integer.toString(second);
        }

        return strSecond;
    }

    public synchronized static int getMilliSecond(long millisecond) {
        long time = sCalendar.getTimeInMillis();
        if (time != millisecond) {
            sCalendar.setTimeInMillis(millisecond);
        }
        int ms = sCalendar.get(Calendar.MILLISECOND);
        return ms;
    }

    public static String getMilliSecond(long millisecond, boolean b) {
        int ms = getMilliSecond(millisecond);
        String strMS;
        if (ms < 10) {
            strMS = "00" + Integer.toString(ms);
        } else if (ms < 100) {
            strMS = "0" + Integer.toString(ms);
        } else {
            strMS = Integer.toString(ms);
        }

        return strMS;
    }

    /**
     *  等同于new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
     */
    public static String yyyyMMddHHmmssSSS(long millisecond) {
        String str = TimeUtil.getYear(millisecond) + STRIKE
                + TimeUtil.getMonth(millisecond, true) + STRIKE
                + TimeUtil.getDay(millisecond, true) + UNDERLINE
                + TimeUtil.getHour(millisecond, true) + COLON
                + TimeUtil.getMinute(millisecond, true) + COLON
                + TimeUtil.getSecond(millisecond, true) + DOT
                + TimeUtil.getMilliSecond(millisecond, true);
        return str;
    }
}
