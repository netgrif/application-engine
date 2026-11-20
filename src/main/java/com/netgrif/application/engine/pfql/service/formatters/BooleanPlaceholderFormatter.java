package com.netgrif.application.engine.pfql.service.formatters;

/**
 * Formatter implementation for Boolean placeholder values in PFQL queries.
 * <p>
 * This formatter handles the conversion of Boolean objects into their string representation
 * for use in query language placeholders. It supports both {@code true} and {@code false} values,
 * converting them to their corresponding string literals.
 * </p>
 *
 * @see QueryLangPlaceholderFormatter
 */
public class BooleanPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return value instanceof Boolean;
    }

    @Override
    public String format(Object value) {
        return String.valueOf(value);
    }
}
