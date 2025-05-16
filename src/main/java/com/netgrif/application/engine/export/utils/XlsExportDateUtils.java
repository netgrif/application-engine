package com.netgrif.application.engine.export.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
public class XlsExportDateUtils {


    public static LocalDate convertToLocalDate(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static LocalDateTime convertToLocalDateTime(Date dateTimeToConvert) {
        return Instant.ofEpochMilli(dateTimeToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDate parseLocalDate(String date, List<String> patterns) {
        if (date == null) {
            throw new IllegalArgumentException("Date is null");
        }

        for (String pattern : patterns) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            try {
                return LocalDate.parse(date, formatter);

            } catch (DateTimeParseException ignored) {
            }
        }

        log.error("Date {} could not be parsed using patterns: {}.", date, patterns);
        return null;
    }

    public static LocalDateTime parseLocalDateTime(String date, List<String> patterns) {
        if (date == null) {
            throw new IllegalArgumentException("Date is null");
        }

        for (String pattern : patterns) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            try {
                return LocalDateTime.parse(date, formatter);

            } catch (DateTimeParseException e) {
                try {
                    return LocalDate.parse(date, formatter).atStartOfDay();

                } catch (DateTimeParseException ignored) {
                }
            }
        }

        log.error("Date {} could not be parsed using patterns: {}.", date, patterns);
        return null;
    }

    public static LocalDate convertToLocalDateIfNeeded(Object date) {
        if (date == null) return null;
        if (date instanceof String) {
            return parseLocalDate((String) date, Arrays.asList("dd.MM.yyyy", "d.M.yyyy"));
        }
        if (date instanceof LocalDate) {
            return (LocalDate) date;
        }
        if (date instanceof Date) {
            return convertToLocalDate((Date) date);
        } else {
            throw new IllegalArgumentException("Error casting field value: Not java.util.Date, java.lang.String, nor java.time.LocalDateTime");
        }
    }

    public static LocalDateTime convertToLocalDateTimeIfNeeded(Object dateTime) {
        if (dateTime == null) return null;
        if (dateTime instanceof String) {
            return parseLocalDateTime((String) dateTime, Arrays.asList("dd.MM.yyyy HH:mm", "d.M.yyyy HH:mm", "dd.MM.yyyy HH:mm:ss", "d.M.yyyy HH:mm:ss", "dd.MM.yyyy", "d.M.yyyy"));
        }
        if (dateTime instanceof LocalDateTime) {
            return (LocalDateTime) dateTime;
        }
        if (dateTime instanceof Date) {
            return convertToLocalDateTime((Date) dateTime);
        } else {
            throw new IllegalArgumentException("Error casting field value: Not java.util.Date, java.lang.String, nor java.time.LocalDateTime");
        }
    }

    public static String dateToString(LocalDate date, String pattern) {
        if (date == null || pattern == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    public static String dateTimeToString(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}
