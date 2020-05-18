package com.netgrif.workflow.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm";
    private static final String DATE_PATTERN = "dd.MM.yyyy";

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

    public static String toString(LocalDateTime localDateTime) {
        return localDateTime.format(dateTimeFormatter);
    }

    public static String toString(LocalDate localDate) {
        return localDate.format(dateFormatter);
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate objectToLocalDate(Object date) {
        if (date == null) {
            return null;
        }

        LocalDate parsed = null;
        if (date instanceof LocalDate) {
            parsed = (LocalDate) date;

        } else if (date instanceof LocalDateTime) {
            parsed = ((LocalDateTime) date).toLocalDate();

        } else if (date instanceof Date) {
            parsed = convertToLocalDate((Date) date);
        }

        return parsed;
    }

    static LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}