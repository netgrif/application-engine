package com.netgrif.application.engine.pfql.service.formatters;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Formatter for handling collections of ObjectId values in PFQL queries.
 * <p>
 * This formatter extends {@link ObjectIdPlaceholderFormatter} to support formatting collections of ObjectId
 * values. It validates that all items in the collection are valid
 * ObjectId values and formats them as a comma-separated list wrapped in brackets.
 * </p>
 * <p>
 * Example transformation: A collection containing ObjectId("507f1f77bcf86cd799439011") and
 * ObjectId("507f191e810c19729de860ea") would be formatted as:
 * ('507f1f77bcf86cd799439011', '507f191e810c19729de860ea')
 * </p>
 *
 * @see ObjectIdPlaceholderFormatter
 */
public class ObjectIdListPlaceholderFormatter extends ObjectIdPlaceholderFormatter {

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
        Collection<?> collOfNumbers = (Collection<?>) value;
        return wrapInBrackets(collOfNumbers.stream()
                .map(item -> super.format(item))
                .collect(Collectors.joining(", ")));
    }
}
