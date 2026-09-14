package com.netgrif.application.engine.pfql.service.formatters;

import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;

import java.util.stream.Collectors;

/**
 * Formatter implementation for CaseRef field placeholder values in PFQL queries.
 * <p>
 * This formatter handles the conversion of case reference fields into their string representation
 * for use in query language placeholders. It supports two types of case reference values:
 * </p>
 * <ul>
 *     <li>{@link CaseField} - Direct case reference fields containing a list of case IDs</li>
 *     <li>{@link MapOptionsField} - Map-based option fields with a "caseref" component</li>
 * </ul>
 * <p>
 * The formatter converts case reference values into a comma-separated list of single-quoted
 * strings enclosed in brackets, e.g., {@code ('case-id-1', 'case-id-2')}.
 * Empty or null values are formatted as empty brackets {@code ()}.
 * </p>
 *
 * @see QueryLangPlaceholderFormatter
 * @see CaseField
 * @see MapOptionsField
 */
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
