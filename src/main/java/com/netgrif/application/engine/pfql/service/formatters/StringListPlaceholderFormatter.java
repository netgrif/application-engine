package com.netgrif.application.engine.pfql.service.formatters;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Formatter for collections of strings in PFQL placeholder substitution.
 * <p>
 * This formatter handles collections of string values by formatting each individual string
 * using the parent {@link StringPlaceholderFormatter} logic and then combining them into
 * a comma-separated list wrapped in brackets.
 * </p>
 * <p>
 * The formatter only supports non-empty collections where all items are supported by the
 * parent string formatter.
 * </p>
 */
public class StringListPlaceholderFormatter extends StringPlaceholderFormatter {

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
