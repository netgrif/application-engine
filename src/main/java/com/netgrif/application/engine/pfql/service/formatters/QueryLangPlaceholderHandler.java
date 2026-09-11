package com.netgrif.application.engine.pfql.service.formatters;

import org.springframework.stereotype.Service;

import java.util.List;

// todo 2483 doc
@Service
public class QueryLangPlaceholderHandler {
    private final List<QueryLangPlaceholderFormatter> formatters;

    public QueryLangPlaceholderHandler() {
        this.formatters = List.of(
                new BooleanPlaceholderFormatter(),
                new NumberPlaceholderFormatter(),
                new NumberListPlaceholderFormatter(),
                new ObjectIdPlaceholderFormatter(),
                new ObjectIdListPlaceholderFormatter(),
                new VersionPlaceholderFormatter(),
                new VersionListPlaceholderFormatter(),
                new DateTimePlaceholderFormatter(),
                new DateTimeListPlaceholderFormatter(),
                new DatePlaceholderFormatter(),
                new DateListPlaceholderFormatter(),
                new StringPlaceholderFormatter(),
                new StringListPlaceholderFormatter(),
                new CaseRefPlaceholderFormatter(),
                new TaskRefPlaceholderFormatter()
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
