package com.netgrif.application.engine.pfql.service.formatters;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Formatter for collections of numeric values in PFQL placeholders.
 * <p>
 * This formatter extends {@link NumberPlaceholderFormatter} to handle collections of numbers.
 * It formats a collection of numeric values by converting each number individually using the
 * parent formatter and joining them with commas, wrapped in brackets.
 * </p>
 * <p>
 * Example: A collection [1, 2.5, 3] would be formatted as "(1, 2.5, 3)"
 * </p>
 */
public class NumberListPlaceholderFormatter extends NumberPlaceholderFormatter {

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
