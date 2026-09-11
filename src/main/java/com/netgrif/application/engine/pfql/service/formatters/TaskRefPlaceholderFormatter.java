package com.netgrif.application.engine.pfql.service.formatters;

import com.netgrif.application.engine.petrinet.domain.dataset.TaskField;

import java.util.stream.Collectors;

// todo 2483 doc
public class TaskRefPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return value instanceof TaskField;
    }

    @Override
    public String format(Object value) {
        TaskField field = (TaskField) value;
        if (field.getValue() == null) {
            return "()";
        }
        return wrapInBrackets(field.getValue().stream()
                .map(this::wrapInSingleQuotes)
                .collect(Collectors.joining(", ")));
    }
}
