package com.netgrif.application.engine.pfql.service.formatters;

import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;

import java.util.stream.Collectors;

// todo 2483 doc
public class CaseRefPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return value instanceof CaseField;
    }

    @Override
    public String format(Object value) {
        CaseField field = (CaseField) value;
        if (field.getValue() == null) {
            return "";
        }
        return wrapInBrackets(field.getValue().stream()
                .map(this::wrapInSingleQuotes)
                .collect(Collectors.joining(", ")));
    }
}
