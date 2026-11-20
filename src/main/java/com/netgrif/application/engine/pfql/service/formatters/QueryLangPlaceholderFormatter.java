package com.netgrif.application.engine.pfql.service.formatters;

/**
 * Interface for formatting placeholder values in PFQL queries.
 * <p>
 * Implementations of this interface are responsible for converting Java objects into their
 * string representations. Each formatter implementation
 * supports specific types of objects and provides custom formatting logic for those types.
 * </p>
 * <p>
 * The formatter provides utility methods for wrapping values in brackets and single quotes,
 * which are commonly needed when constructing query strings.
 * </p>
 *
 * @see QueryLangPlaceholderHandler
 */
public interface QueryLangPlaceholderFormatter {
    boolean supports(Object value);
    String format(Object value);

    default String wrapInBrackets(String valueInBrackets) {
        if (valueInBrackets == null) {
            return "()";
        }
        return "(" + valueInBrackets + ")";
    }

    default String wrapInSingleQuotes(Object valueToWrap) {
        if (valueToWrap == null) {
            return "''";
        }
        return "'" + valueToWrap + "'";
    }
}