package com.netgrif.application.engine.pfql.service.formatters;

import org.bson.types.ObjectId;

/**
 * Formatter for converting MongoDB {@link ObjectId} instances into PFQL query string format.
 * <p>
 * This formatter is part of the PFQL placeholder formatting system.
 * It handles the conversion of {@link ObjectId} objects into their hexadecimal string representation
 * wrapped in single quotes, making them suitable for use in MongoDB queries generated from PFQL expressions.
 * </p>
 * <p>
 * When a PFQL query contains placeholder values that are {@link ObjectId} instances, this formatter
 * ensures they are properly converted to their string format (e.g., {@code '507f1f77bcf86cd799439011'})
 * </p>
 *
 * @see QueryLangPlaceholderFormatter
 * @see ObjectId
 */
public class ObjectIdPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return value instanceof ObjectId;
    }

    @Override
    public String format(Object value) {
        return wrapInSingleQuotes(((ObjectId) value).toHexString());
    }
}
