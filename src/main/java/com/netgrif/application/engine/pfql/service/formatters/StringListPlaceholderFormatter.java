package com.netgrif.application.engine.pfql.service.formatters;

import java.util.Collection;
import java.util.stream.Collectors;

// todo 2483 doc
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
