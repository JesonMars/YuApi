package com.pingo.yuapi.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 日期时间工具类
 * 用于处理LocalDateTime与字符串、时间戳之间的转换
 */
public class DateUtils {

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = new ArrayList<>();
    private static final List<DateTimeFormatter> DATE_FORMATTERS = new ArrayList<>();
    private static final List<DateTimeFormatter> TIME_FORMATTERS = new ArrayList<>();

    static {
        // 完整的日期时间格式
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        DATE_TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));

        // 日期格式
        DATE_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DATE_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 时间格式
        TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("HH:mm:ss"));
        TIME_FORMATTERS.add(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * 将字符串转换为LocalDateTime
     * 支持格式：
     * 1. yyyy-MM-dd HH:mm:ss
     * 2. yyyy-MM-dd HH:mm
     * 3. yyyy-MM-dd (返回当天的00:00:00)
     * 4. HH:mm:ss (返回当天的HH:mm:ss)
     * 5. HH:mm (返回当天的HH:mm:00)
     * 
     * @param str 时间字符串
     * @return LocalDateTime对象
     * @throws IllegalArgumentException 如果格式无法解析
     */
    public static LocalDateTime parse(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        String timeStr = str.trim();

        // 1. 尝试完整的日期时间格式
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(timeStr, formatter);
            } catch (DateTimeParseException e) {
                // continue
            }
        }

        // 2. 尝试日期格式 (补全为当天开始时间)
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(timeStr, formatter);
                return date.atStartOfDay();
            } catch (DateTimeParseException e) {
                // continue
            }
        }

        // 3. 尝试时间格式 (补全为当天日期)
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                LocalTime time = LocalTime.parse(timeStr, formatter);
                return LocalDateTime.of(LocalDate.now(), time);
            } catch (DateTimeParseException e) {
                // continue
            }
        }

        throw new IllegalArgumentException("无法解析的时间格式: " + str);
    }

    /**
     * 时间戳转LocalDateTime
     * 
     * @param timestamp 毫秒级时间戳
     * @return LocalDateTime对象
     */
    public static LocalDateTime fromTimestamp(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转时间戳
     * 
     * @param dateTime LocalDateTime对象
     * @return 毫秒级时间戳
     */
    public static Long toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 格式化为字符串
     * 
     * @param dateTime LocalDateTime对象
     * @param pattern  格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化为标准字符串 (yyyy-MM-dd HH:mm:ss)
     * 
     * @param dateTime LocalDateTime对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, "yyyy-MM-dd HH:mm:ss");
    }
}
