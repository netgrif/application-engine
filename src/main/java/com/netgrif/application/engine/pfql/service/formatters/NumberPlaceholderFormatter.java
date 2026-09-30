package com.netgrif.application.engine.pfql.service.formatters;

/**
 * Formatter for converting Number values to their string representation in PFQL queries.
 * <p>
 * This formatter handles all numeric types (Integer, Long, Double, Float, etc.) by converting
 * them to their string representation using {@link String#valueOf(Object)}. It is used during
 * placeholder substitution in PFQL query processing to safely embed numeric values into queries.
 * </p>
 */
public class NumberPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return value instanceof Number;
    }

    @Override
    public String format(Object value) {
        return String.valueOf(value);
    }
}
