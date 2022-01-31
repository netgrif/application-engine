package com.netgrif.application.engine.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm";
    private static final String DATE_PATTERN = "dd.MM.yyyy";
    private static final String DATE_PATTERN_dd_MMM_yyyy = "dd-MMM-yyyy";

    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    public static final DateTimeFormatter dd_MMM_yyyy = DateTimeFormatter.ofPattern(DATE_PATTERN_dd_MMM_yyyy);

    public static String toString(LocalDateTime localDateTime) {
        return localDateTime.format(dateTimeFormatter);
    }

    public static String toString(LocalDate localDate) {
        return localDate.format(dateFormatter);
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static ZonedDateTime localDateTimeToZonedDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        return ZonedDateTime.from(localDateTime.atZone(zoneId));
    }

    public static Date localDateToDate(LocalDate dateToConvert) {
        return Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static ZonedDateTime localDateToZonedDate(LocalDate dateToConvert, ZoneId zoneId) {
        return ZonedDateTime.from(dateToConvert.atStartOfDay().atZone(zoneId));
    }

    static LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}