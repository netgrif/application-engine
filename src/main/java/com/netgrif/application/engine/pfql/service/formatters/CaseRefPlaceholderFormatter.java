package com.netgrif.application.engine.pfql.service.formatters;

import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;

import java.util.stream.Collectors;

// todo 2483 doc
public class CaseRefPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return isValueCaseRef(value) || isOptionsCaseRef(value);
    }

    @Override
    public String format(Object value) {
        if (isValueCaseRef(value)) {
            CaseField field = (CaseField) value;
            if (field.getValue() == null) {
                return "()";
            }
            return wrapInBrackets(field.getValue().stream()
                    .map(this::wrapInSingleQuotes)
                    .collect(Collectors.joining(", ")));
        }

        MapOptionsField<?, ?> field = (MapOptionsField<?, ?>) value;
        if (field.getOptions() == null) {
            return "()";
        }

        return wrapInBrackets(field.getOptions().keySet().stream()
                .map(this::wrapInSingleQuotes)
                .collect(Collectors.joining(", ")));
    }

    protected boolean isValueCaseRef(Object value) {
        return value instanceof CaseField;
    }

    protected boolean isOptionsCaseRef(Object value) {
        boolean isOptionsField = value instanceof MapOptionsField;
        if (isOptionsField) {
            MapOptionsField<?, ?> field = (MapOptionsField<?, ?>) value;
            Component component = field.getComponent();
            if (component == null) {
                return false;
            }
            return component.getName() != null && component.getName().equals("caseref");
        }
        return false;
    }
}
