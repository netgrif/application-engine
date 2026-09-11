package com.netgrif.application.engine.pfql.service.formatters;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Formatter for collections of date values in PFQL placeholders.
 * <p>
 * This formatter extends {@link DatePlaceholderFormatter} to handle collections of date objects.
 * It validates that all items in the collection are supported date types and formats them as a
 * comma-separated list wrapped in brackets suitable for MongoDB query syntax.
 * </p>
 * <p>
 * Example output: {@code [2023-01-15T10:30:00Z, 2023-02-20T14:45:00Z, 2023-03-25T08:15:00Z]}
 * </p>
 *
 * @see DatePlaceholderFormatter
 */
public class DateListPlaceholderFormatter extends DatePlaceholderFormatter {

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
        Collection<?> collOfDates = (Collection<?>) value;
        return wrapInBrackets(collOfDates.stream()
                .map(item -> super.format(item))
                .collect(Collectors.joining(", ")));
    }
}
