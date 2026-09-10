package com.netgrif.application.engine.pfql.service.formatters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

// todo 2483 doc
public class DatePlaceholderFormatter implements QueryLangPlaceholderFormatter {

    /**
     * Regex pattern derived from the {@code DATE} token defined in {@code QueryLang.g4}:
     * <pre>
     * DATE: DIGIT DIGIT DIGIT DIGIT '-' ('0' [1-9] | '1' [0-2]) '-' ('0' [1-9] | [12] DIGIT | '3' [01])
     * </pre>
     * Example: {@code 2020-03-03}
     * <p>
     * <b>Note:</b> If the grammar changes, this pattern must be updated accordingly.
     * </p>
     */
    protected static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])");
    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean supports(Object value) {
        return isOfLocalDateType(value) || isOfStringType(value);
    }

    @Override
    public String format(Object value) {
        if (isOfLocalDateType(value)) {
            return ((LocalDate) value).format(DATE_FORMATTER);
        }
        return (String) value;
    }

    protected boolean isOfStringType(Object value) {
        return value instanceof String && DATE_PATTERN.matcher((String) value).matches();
    }

    protected boolean isOfLocalDateType(Object value) {
        return value instanceof LocalDate;
    }
}
