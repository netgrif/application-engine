package com.netgrif.application.engine.pfql.service.formatters;

/**
 * Formatter implementation for handling String value placeholders in PFQL queries.
 * <p>
 * This formatter is responsible for converting String placeholder values into properly formatted
 * query string representations by wrapping them in single quotes. It is part of the placeholder
 * handling mechanism that ensures type-safe query construction.
 * </p>
 * <p>
 * The formatter implements {@link QueryLangPlaceholderFormatter} and is automatically selected
 * by {@link com.netgrif.application.engine.pfql.service.formatters.QueryLangPlaceholderHandler}
 * when processing String-typed placeholder values during query evaluation.
 * </p>
 *
 * @see QueryLangPlaceholderFormatter
 * @see com.netgrif.application.engine.pfql.service.formatters.QueryLangPlaceholderHandler
 */
public class StringPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return value instanceof String;
    }

    @Override
    public String format(Object value) {
        return wrapInSingleQuotes(value);
    }
}
