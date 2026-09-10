package com.netgrif.application.engine.pfql.service.formatters;

// todo 2483 doc
public interface QueryLangPlaceholderFormatter {
    boolean supports(Object value);
    String format(Object value);

    default String wrapInBrackets(String valueInBrackets) {
        if (valueInBrackets == null) {
            return "()";
        }
        return "(" + valueInBrackets + ")";
    }

    default String wrapInSingleQuotes(Object valueToWrap) {
        if (valueToWrap == null) {
            return "''";
        }
        return "'" + valueToWrap + "'";
    }
}