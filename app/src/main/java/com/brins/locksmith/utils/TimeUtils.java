package com.brins.locksmith.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * TimeUtils
 */
public class TimeUtils {

    public static final SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    public static final int SECONDS_IN_DAY = 60 * 60 * 24;
    public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;
    public static final long MILLIS_IN_HOUR = 1000L * 60 * 60;

    public static final int NATIVE = 0;
    public static final int TODAY = 1;
    public static final int YESTODAY = 2; // 昨天
    public static final int THE_DAY_BEFORE_YESTERDA = 3; // 前天


    private TimeUtils() {
        throw new AssertionError();
    }

    public static boolean isSameDayOfMillis(final long ms1, final long ms2) {
        final long interval = Math.abs(ms1 - ms2);
        return (interval < MILLIS_IN_DAY)
                && (toDay(ms1) == toDay(ms2));
    }

    public static long toDay(long millis) {
        return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_DAY;
    }

    public static long toHour(long millis) {
        return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_HOUR;
    }

    /**
     * long time to string
     *
     * @param timeInMillis
     * @param dateFormat
     * @return
     */
    public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(timeInMillis));
    }

    /**
     * long time to string, format is {@link #DEFAULT_DATE_FORMATTER}
     *
     * @param timeInMillis
     * @return
     */
    public static String getTime(long timeInMillis) {
        return getTime(timeInMillis, DEFAULT_DATE_FORMATTER);
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static long getCurrentTimeInLong() {
        return System.currentTimeMillis();
    }

    /**
     * get current time in milliseconds, format is {@link #DEFAULT_DATE_FORMATTER}
     *
     * @return
     */
    public static String getCurrentTimeInString() {
        return getTime(getCurrentTimeInLong());
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static String getCurrentTimeInString(SimpleDateFormat dateFormat) {
        return getTime(getCurrentTimeInLong(), dateFormat);
    }


    /**
     * yyyy-MM-dd HH:mm:ss 格式的时间
     */
    public static String getSimpleDate() {
        long currentTime = System.currentTimeMillis();
        return getSimpleDateByCurrentTime(currentTime);
    }

    /**
     * 通过格林威治时间 转成 yyyy-MM-dd HH:mm:ss 格式的时间
     */
    public static String getSimpleDateByCurrentTime(long currentTimeMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(currentTimeMillis);
        return formatter.format(date);
    }

    /**
     * 通过格林威治时间 转成 yyyy-MM-dd HH:mm:ss 格式的时间
     */
    public static String getDateByCurrentTime(long currentTimeMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(currentTimeMillis);
        return formatter.format(date);
    }

    /**
     * yyyy-MM-dd HH:mm:ss 数组 [yyyy,mm,dd,hh,mm,ss] 格式转换 1300000000000
     */
    public static long getCurrentTimeMillisBySimpleDate(String... strings) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Integer.valueOf(strings[0]), Integer.valueOf(strings[1]), Integer.valueOf(strings[2]),
                Integer.valueOf(strings[3]), Integer.valueOf(strings[4]), Integer.valueOf(strings[5]));
        TimeZone tz = TimeZone.getDefault();
        calendar.setTimeZone(tz);
        return calendar.getTimeInMillis();
    }

    /**
     * yyyy-MM-dd HH:mm:ss 转成数组 [yyyy,mm,dd,hh,mm,ss]
     */
    public static String[] getSimpleDateByte(String create_time) {
        String[] timeSplit = create_time.split(" ");
        String[] dataSplit = timeSplit[0].split("-");
        String[] hourSplit = timeSplit[1].split(":");
        return concatAll(dataSplit, hourSplit);
    }


    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * 通过格林威治时间判断当前时间状态：今天、昨天、前天
     *
     * @param currentTimeMillis
     * @return
     */
    public static int getTimeTypeByCurrentTimeMillis(long currentTimeMillis) {
        int timeType = NATIVE;
        try {
            String create_time = getSimpleDateByCurrentTime(currentTimeMillis);
            String[] dataString = getSimpleDateByte(create_time);
            String[] currentDataString = getSimpleDateByte(getSimpleDate());
            long downloadTime = getCurrentTimeMillisBySimpleDate(dataString);
            long timestamp = getCurrentTimeMillisBySimpleDate(currentDataString) - downloadTime;
            if (0 < timestamp && timestamp < MILLIS_IN_DAY) {
                timeType = TODAY;
            } else if (MILLIS_IN_DAY < timestamp && timestamp < MILLIS_IN_DAY * 2) {
                timeType = YESTODAY;
            } else if (MILLIS_IN_DAY < timestamp && timestamp < MILLIS_IN_DAY * 3) {
                timeType = THE_DAY_BEFORE_YESTERDA;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeType;
    }

    /**
     * 以此来计算两个时间相差天数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 相差天数  0代表同一天
     */
    public static int calcDifferenceDays(long startTime, long endTime) throws Exception {
        if (startTime <= 0 || endTime <= 0) {
            throw new Exception("startTime or endTime < 0");
        }
        return Math.abs((int) (toDay(startTime) - toDay(endTime)));
    }

    /**
     * 以此来计算两个时间相差小时数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 相差天数  0代表同一小时
     */
    public static int calcDifferenceHours(long startTime, long endTime) throws Exception {
        if (startTime <= 0 || endTime <= 0) {
            throw new Exception("startTime or endTime < 0");
        }
        return Math.abs((int) (toHour(startTime) - toHour(endTime)));
    }

    /**
     * 获取今天日期，如2016-10-25
     */
    public static String getTodayDate() {
        SimpleDateFormat sdf = getDateFormat();
        return sdf.format(new Date());
    }

    /**
     * 获取当前日期的整数格式，如20180803
     *
     * @return
     */
    public static int getTodayDateInt() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        String dateStr = df.format(new Date());
        return Integer.parseInt(dateStr);
    }

    /**
     * 获取通用的日期格式
     */
    public static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    /**
     * 获取前后两天的间隔（向上取整）
     **/
    public static int calcDifferenceDaysCeil(long startTime, long endTime) throws Exception {
        if (startTime <= 0 || endTime <= 0) {
            throw new Exception("startTime or endTime < 0");
        }
        return Math.abs((int) (toDayCeil(startTime) - toDayCeil(endTime)));
    }

    private static double toDayCeil(long millis) {
        return Math.ceil((double) (millis + TimeZone.getDefault().getOffset(millis)) / (double) MILLIS_IN_DAY);
    }

    public static int getHourOfDay() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    public static boolean isToday(long j) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        long timeInMillis = instance.getTimeInMillis();
        if (j <= timeInMillis || j >= timeInMillis + 86400000) {
            return false;
        }
        return true;
    }

    public static String fomatTime(long j) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(j));
    }

    public static long getTodayStart() {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTimeInMillis();
    }

    public static long getTomorrowStart() {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTimeInMillis() + 86400000;
    }

    public static long getClockTime(int i) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, i);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTimeInMillis();
    }

    public static long minToMs(long j) {
        return (60 * j) * 1000;
    }

    /**
     * 把毫秒倒计时转换成HH:mm:ss的格式
     */
    public static String formatCountDownTimeStrBymmss(long millisecond) {
        int minute = 0;
        int second = (int) (millisecond / 1000);
        if (second >= 60) {
            minute = second / 60;
            second = second % 60;
        }
        if (minute >= 60) {
            minute = minute % 60;
        }

        StringBuilder sb = new StringBuilder();

        if (minute < 10) {
            sb.append(0);
        }
        sb.append(minute);
        sb.append(":");
        if (second < 10) {
            sb.append(0);
        }
        sb.append(second);

        return sb.toString();
    }
}
