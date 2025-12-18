package com.netgrif.application.engine.elastic.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class ElasticsearchQuerySanitizer {

    public static final String[] RESERVED_CHARACTERS_TO_ESCAPE = {"\\", "+", "-", "=", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "/", "AND", "OR", "NOT", " "};
    public static final String[] RESERVED_CHARACTERS_TO_REMOVE = {">", "<"};
    public static final Map<String, String> RESERVED_KEYWORDS = prepareReservedKeywords();

    public static String sanitize(String query) {
        return sanitize(query, null);
    }

    public static String sanitize(String query, String[] exclude) {
        Map<String, String> keywordsToEscape = excludeKeywords(exclude);
        String sanitized = keywordsToEscape.entrySet().stream()
                .reduce(query, (q, entry) -> StringUtils.replace(q, entry.getKey(), entry.getValue()), (q1, q2) -> q2);
        log.trace("Sanitized query: {}", sanitized);
        return sanitized;
    }

    protected static Map<String, String> prepareReservedKeywords() {
        if (RESERVED_CHARACTERS_TO_ESCAPE == null || RESERVED_CHARACTERS_TO_REMOVE == null) {
            log.error("Set of reserved characters to escape or remove are null");
            return new HashMap<>();
        }
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
