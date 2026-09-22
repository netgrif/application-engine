package com.netgrif.application.engine.pfql.service.formatters;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Formatter for collections of version values in PFQL queries.
 * <p>
 * This formatter extends {@link VersionPlaceholderFormatter} to handle collections of version objects.
 * It validates that all items in the collection are supported version values and formats them as a
 * comma-separated list wrapped in brackets.
 * </p>
 * <p>
 * The formatter only supports non-empty collections where every element is a valid version value
 * as determined by the parent {@link VersionPlaceholderFormatter#supports(Object)} method.
 * </p>
 *
 * @see VersionPlaceholderFormatter
 */
public class VersionListPlaceholderFormatter extends VersionPlaceholderFormatter {

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
        Collection<?> collOfStrings = (Collection<?>) value;
        return wrapInBrackets(collOfStrings.stream()
                .map(item -> super.format(item))
                .collect(Collectors.joining(", ")));
    }
}
