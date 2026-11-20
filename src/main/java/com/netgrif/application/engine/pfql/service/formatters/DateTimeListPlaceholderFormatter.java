package com.netgrif.application.engine.pfql.service.formatters;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Formatter for handling collections of date-time values in PFQL placeholders.
 * <p>
 * This formatter extends {@link DateTimePlaceholderFormatter} to support formatting of collections
 * containing date-time objects. It validates that all items in the collection are supported date-time
 * types and formats them into a bracketed, comma-separated string representation suitable for
 * MongoDB queries.
 * </p>
 * <p>
 * The formatter only supports non-empty collections where every element passes the parent class's
 * {@link DateTimePlaceholderFormatter#supports(Object)} validation.
 * </p>
 */
public class DateTimeListPlaceholderFormatter extends DateTimePlaceholderFormatter {

    @Override
    @SuppressWarnings("Convert2MethodRef") // method reference does not work to super calls
    public boolean supports(Object value) {
        return value instanceof Collection<?>
                && !((Collection<?>) value).isEmpty()
                && ((Collection<?>) value).stream().allMatch(item -> super.supports(item));
    }

    @Override
    @SuppressWarnings("Convert2MethodRef") // method reference does not work to super calls
    public String format(Object value) {
        Collection<?> collOfDateTimes = (Collection<?>) value;
        return wrapInBrackets(collOfDateTimes.stream()
                .map(item -> super.format(item))
                .collect(Collectors.joining(", ")));
    }
}
