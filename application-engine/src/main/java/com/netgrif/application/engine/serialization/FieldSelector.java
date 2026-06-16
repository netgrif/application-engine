package com.netgrif.application.engine.serialization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class FieldSelector {
    private final Set<String> fields;
    private final Map<String, FieldSelector> nested;

    private FieldSelector() {
        this(null, null);
    }

    private FieldSelector(Set<String> fields, Map<String, FieldSelector> nested) {
        this.fields = fields;
        this.nested = nested;
    }

    public static FieldSelector parse(String spec) {
        Set<String> fields = new HashSet<>();
        Map<String, FieldSelector> nested = new HashMap<>();

        if (spec == null || spec.isBlank()) {
            return new FieldSelector();
        }

        int i = 0;
        while (i < spec.length()) {
            int commaIdx = nextCommaOrEnd(spec, i);
            int parenIdx = spec.indexOf('(', i);

            if (parenIdx != -1 && parenIdx < commaIdx) {
                String name = spec.substring(i, parenIdx);
                int closeParen = findMatchingParen(spec, parenIdx);
                String inner = spec.substring(parenIdx + 1, closeParen);
                nested.put(name, parse(inner));
                i = closeParen + 1;
                if (i < spec.length() && spec.charAt(i) == ',') i++;
            } else {
                fields.add(spec.substring(i, commaIdx));
                i = commaIdx + 1;
            }
        }
        return new FieldSelector(fields, nested);
    }

    public boolean includes(String field) {
        if (fields == null) return true;
        if (fields.contains(field)) return true;
        return nested != null && nested.containsKey(field);
    }

    public boolean includeAll() {
        return fields == null;
    }

    public FieldSelector nested(String field) {
        if (nested == null || !nested.containsKey(field)) {
            return new FieldSelector(null, null); // no restriction on nested level either
        }
        return nested.get(field);
    }

    private static int nextCommaOrEnd(String spec, int from) {
        int depth = 0;
        for (int i = from; i < spec.length(); i++) {
            char c = spec.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == ',' && depth == 0) return i;
        }
        return spec.length();
    }

    private static int findMatchingParen(String spec, int openIdx) {
        int depth = 0;
        for (int i = openIdx; i < spec.length(); i++) {
            char c = spec.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        throw new IllegalArgumentException("Unmatched '(' in fields selector: " + spec);
    }
}
