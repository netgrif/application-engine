package com.netgrif.application.engine.elastic.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * The ElasticsearchQuerySanitizer class is responsible for sanitizing Elasticsearch queries
 * by escaping or removing reserved characters and keywords. This is essential to ensure proper
 * handling of Elasticsearch queries and to prevent syntax issues caused by special characters or
 * reserved words.
 * <p>
 * This class provides utility methods to sanitize query strings by escaping predefined reserved
 * characters, removing certain reserved characters, and excluding specific keywords if provided.
 * The reserved characters and keywords are predefined and managed internally.
 */
@Slf4j
public class ElasticsearchQuerySanitizer {

    public static final String[] RESERVED_CHARACTERS_TO_ESCAPE = {"\\", "+", "-", "=", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "/", "AND", "OR", "NOT", " "};
    public static final String[] RESERVED_CHARACTERS_TO_REMOVE = {">", "<"};
    public static final Map<String, String> RESERVED_KEYWORDS = prepareReservedKeywords();

    /**
     * Sanitizes the provided Elasticsearch query string by escaping or removing certain reserved
     * characters and excluding specific keywords if applicable.
     * <p>
     * This method applies default sanitization rules and does not consider keyword exclusions.
     *
     * @param query the Elasticsearch query string to sanitize, such as a search query or filter.
     *              It must not be null to ensure proper sanitization.
     * @return the sanitized query string with reserved characters handled appropriately.
     * If the input is empty or null, the behavior depends on the implemented sanitization logic.
     */
    public static String sanitize(String query) {
        return sanitize(query, null);
    }

    /**
     * Sanitizes the given query string by replacing reserved keywords with their sanitized equivalents,
     * excluding the specified keywords from sanitization.
     *
     * @param query   the query string to sanitize, which may contain reserved characters and keywords.
     *                This string must not be null.
     * @param exclude an array of keywords to exclude from sanitization. If null or empty, all reserved
     *                keywords will be considered for sanitization.
     * @return the sanitized query string with reserved keywords appropriately replaced, and excluded
     * keywords untouched.
     */
    public static String sanitize(String query, String[] exclude) {
        if (query == null || query.isBlank()) {
            return query;
        }
        Map<String, String> keywordsToEscape = excludeKeywords(exclude);
        String sanitized = StringUtils.replaceEach(query,
                keywordsToEscape.keySet().toArray(new String[0]),
                keywordsToEscape.values().toArray(new String[0]));
        log.trace("Sanitized query: {}", sanitized);
        return sanitized;
    }

    protected static Map<String, String> prepareReservedKeywords() {
        Map<String, String> result = new HashMap<>();
        for (String reservedString : RESERVED_CHARACTERS_TO_ESCAPE) {
            String escaped = Arrays.stream(reservedString.split(""))
                    .map(c -> "\\" + c)
                    .collect(Collectors.joining(""));
            result.put(reservedString, escaped);
        }
        for (String reservedString : RESERVED_CHARACTERS_TO_REMOVE) {
            result.put(reservedString, "\\ ");
        }

        return Collections.unmodifiableMap(result);
    }

    protected static Map<String, String> excludeKeywords(String[] exclude) {
        if (exclude == null || exclude.length == 0) {
            return RESERVED_KEYWORDS;
        }
        Map<String, String> keywordsToEscape = new HashMap<>(RESERVED_KEYWORDS);
        for (String toExclude : exclude) {
            if (RESERVED_KEYWORDS.containsKey(toExclude)) {
                keywordsToEscape.remove(toExclude);
            }
        }
        return Collections.unmodifiableMap(keywordsToEscape);
    }


}
