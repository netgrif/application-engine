package com.netgrif.application.engine.pfql.service.formatters;

import com.netgrif.application.engine.petrinet.domain.dataset.TaskField;

import java.util.stream.Collectors;

/**
 * Formatter for TaskField placeholders in PFQL queries.
 * <p>
 * This formatter handles the conversion of {@link TaskField} values into properly formatted
 * query strings. It wraps task reference values in brackets and quotes.
 * </p>
 * <p>
 * If the TaskField contains null values, it returns an empty bracket pair "()".
 * Otherwise, it formats each task reference value by wrapping it in single quotes and
 * joining them with commas within brackets.
 * </p>
 */
public class TaskRefPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return value instanceof TaskField;
    }

    @Override
    public String format(Object value) {
        TaskField field = (TaskField) value;
        if (field.getValue() == null) {
            return "()";
        }
        return wrapInBrackets(field.getValue().stream()
                .map(this::wrapInSingleQuotes)
                .collect(Collectors.joining(", ")));
    }
}
