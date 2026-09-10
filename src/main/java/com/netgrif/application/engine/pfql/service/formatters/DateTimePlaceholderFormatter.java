package com.netgrif.application.engine.pfql.service.formatters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.regex.Pattern;

// todo 2483 doc
public class DateTimePlaceholderFormatter implements QueryLangPlaceholderFormatter {

    /**
     * Regex pattern derived from the {@code DATETIME} token defined in {@code QueryLang.g4}:
     * <pre>
     * DATETIME: DATE 'T' ([01] DIGIT | '2' [0-3]) ':' [0-5] DIGIT ':' [0-5] DIGIT ('.' DIGIT+)?
     * </pre>
     * Example: {@code 2020-03-03T20:00:00} or {@code 2020-03-03T20:00:00.055}
     * <p>
     * <b>Note:</b> If the grammar changes, this pattern must be updated accordingly.
     * </p>
     */
    protected static final Pattern DATETIME_PATTERN = Pattern.compile(
            "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])T([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(\\.\\d+)?"
    );
    protected static final DateTimeFormatter DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter();

    @Override
    public boolean supports(Object value) {
        return isOfLocalDateTimeType(value) || isOfDateType(value) || isOfStringType(value);
    }

    @Override
    public String format(Object value) {
        if (isOfLocalDateTimeType(value)) {
            return ((LocalDateTime) value).format(DATETIME_FORMATTER);
        } else if (isOfDateType(value)) {
            return ((Date) value).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(DATETIME_FORMATTER);
        }

        return (String) value;
    }

    protected boolean isOfStringType(Object value) {
        return value instanceof String && DATETIME_PATTERN.matcher((String) value).matches();
    }

    protected boolean isOfLocalDateTimeType(Object value) {
        return value instanceof LocalDateTime;
    }

    protected boolean isOfDateType(Object value) {
        return value instanceof Date;
    }
}
