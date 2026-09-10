package com.netgrif.application.engine.pfql.service.formatters;

// todo 2483 doc
public class BooleanPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return value instanceof Boolean;
    }

    @Override
    public String format(Object value) {
        return String.valueOf(value);
    }
}
