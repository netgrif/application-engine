package com.netgrif.application.engine.pfql.service.formatters;

import org.springframework.stereotype.Service;

import java.util.List;

// todo 2483 doc
@Service
public class QueryLangPlaceholderHandler {
    private final List<QueryLangPlaceholderFormatter> formatters;

    public QueryLangPlaceholderHandler() {
        this.formatters = List.of(
                new CaseRefPlaceholderFormatter(),
                new TaskRefPlaceholderFormatter(),
                new BooleanPlaceholderFormatter(),
                new NumberPlaceholderFormatter(),
                new NumberListPlaceholderFormatter(),
                new ObjectIdPlaceholderFormatter(),
                new ObjectIdListPlaceholderFormatter(),
                new DateTimePlaceholderFormatter(),
                new DateTimeListPlaceholderFormatter(),
                new DatePlaceholderFormatter(),
                new DateListPlaceholderFormatter(),
                new StringPlaceholderFormatter(),
                new StringListPlaceholderFormatter()
        );
    }

    public String format(Object value) {
        return formatters.stream()
            .filter(formatter -> formatter.supports(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported placeholder value: " + value))
            .format(value);
    }
}
