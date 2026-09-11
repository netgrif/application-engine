package com.netgrif.application.engine.pfql.service.formatters;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for formatting placeholder values in PFQL queries based on their type.
 * <p>
 * This handler maintains a list of specialized formatters for different data types and selects
 * the appropriate formatter based on the runtime type of the value being formatted. Supported
 * types include primitives (boolean, number), collections (lists), temporal types (date, datetime),
 * identifiers (ObjectId, version), and reference types (case ref, task ref).
 * </p>
 */
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

    /**
     * Formats a placeholder value according to its runtime type.
     * <p>
     * This method iterates through the registered formatters and uses the first one that
     * supports the given value type. The formatted result is a string representation suitable
     * for use in MongoDB queries.
     * </p>
     *
     * @param value the placeholder value to format; can be of various types including primitives,
     *              collections, temporal types, or reference types
     * @return the formatted string representation of the value suitable for MongoDB queries
     * @throws IllegalArgumentException if no formatter supports the given value type
     */
    public String format(Object value) {
        return formatters.stream()
            .filter(formatter -> formatter.supports(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported placeholder value: " + value))
            .format(value);
    }
}
