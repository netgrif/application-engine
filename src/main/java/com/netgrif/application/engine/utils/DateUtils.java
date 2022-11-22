package com.netgrif.application.engine.utils;

import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import com.netgrif.application.engine.workflow.domain.Case;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    public static final DateTimeFormatter[] DATE_PATTERNS = {
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.BASIC_ISO_DATE,
            DateTimeFormatter.ISO_DATE
    };
    public static final DateTimeFormatter[] DATE_TIME_PATTERNS = {
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_INSTANT
    };

    public static Optional<LocalDate> parseDate(String defaultValueString) {
        for (DateTimeFormatter pattern : DATE_PATTERNS) {
            try {
                return Optional.of(LocalDate.parse(defaultValueString, pattern));
            } catch (DateTimeParseException ignored) {}
        }
        return Optional.empty();
    }

    public static Optional<LocalDateTime> parseDateTime(String defaultValueString) {
        for (DateTimeFormatter pattern : DATE_TIME_PATTERNS) {
            try {
                return Optional.of(LocalDateTime.parse(defaultValueString, pattern));
            } catch (DateTimeParseException ignored) {}
        }
        return Optional.empty();
    }
}