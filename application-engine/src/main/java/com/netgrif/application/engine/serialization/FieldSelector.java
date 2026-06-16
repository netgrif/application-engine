package com.netgrif.application.engine.serialization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A utility class for parsing and managing field selection specifications in JSON serialization.
 * <p>
 * This class enables selective field inclusion during serialization by parsing a field specification
 * string that supports both simple field names and nested field selections using parentheses notation.
 * </p>
 * <p>
 * Field specification format:
 * <ul>
 *   <li>Simple fields: {@code "field1,field2,field3"}</li>
 *   <li>Nested fields: {@code "field1,field2(nestedField1,nestedField2),field3"}</li>
 *   <li>Deep nesting: {@code "field1(nested1(deepNested1,deepNested2))"}</li>
 * </ul>
 * </p>
 * <p>
 * When no specification is provided (null or blank), the selector allows all fields (unrestricted mode).
 * </p>
 *
 * @see LocalizedEventOutcomeSerializer
 * @see DynamicFieldSerializer
 * @see FieldSelectorHolder
 */

public final class FieldSelector {
    /**
     * Set of field names to include at the current level of serialization.
     * When null, all fields are included (unrestricted mode).
     */
    private final Set<String> fields;

    /**
     * Map of field names to their corresponding nested {@link FieldSelector} instances.
     * Enables hierarchical field selection for complex object structures.
     * When null, no nested field restrictions are applied.
     */
    private final Map<String, FieldSelector> nested;

    /**
     * Creates a new FieldSelector in unrestricted mode.
     * <p>
     * This constructor creates a selector that allows all fields to be included
     * during serialization without any restrictions.
     * </p>
     */
    private FieldSelector() {
        this(null, null);
    }

    /**
     * Creates a new FieldSelector with specified field selections and nested selectors.
     *
     * @param fields the set of field names to include at this level, or null for unrestricted mode
     * @param nested the map of field names to their nested selectors, or null if no nested selections
     */
    private FieldSelector(Set<String> fields, Map<String, FieldSelector> nested) {
        this.fields = fields;
        this.nested = nested;
    }

    /**
     * Parses a field specification string and creates a corresponding FieldSelector.
     * <p>
     * The parser supports:
     * <ul>
     *   <li>Comma-separated field names: {@code "field1,field2,field3"}</li>
     *   <li>Nested field selections using parentheses: {@code "field1(nested1,nested2)"}</li>
     *   <li>Multiple levels of nesting: {@code "field1(nested1(deepNested1))"}</li>
     * </ul>
     * </p>
     * <p>
     * If the specification is null or blank, an unrestricted selector is returned that allows all fields.
     * </p>
     *
     * @param spec the field specification string to parse, may be null or blank
     * @return a FieldSelector instance representing the parsed specification
     * @throws IllegalArgumentException if the specification contains unmatched parentheses
     */
    public static FieldSelector parse(String spec) {
        Set<String> fields = new HashSet<>();
        Map<String, FieldSelector> nested = new HashMap<>();

        if (spec == null || spec.isBlank()) {
            return new FieldSelector();
        }

        int i = 0;
        while (i < spec.length()) {
            int commaIdx = nextCommaOrEnd(spec, i);
            int parenthesisIdx = spec.indexOf('(', i);

            if (parenthesisIdx != -1 && parenthesisIdx < commaIdx) {
                String name = spec.substring(i, parenthesisIdx);
                int closeParenthesisIdx = findMatchingParen(spec, parenthesisIdx);
                String inner = spec.substring(parenthesisIdx + 1, closeParenthesisIdx);
                nested.put(name, parse(inner));
                i = closeParenthesisIdx + 1;
                if (i < spec.length() && spec.charAt(i) == ',') i++;
            } else {
                fields.add(spec.substring(i, commaIdx));
                i = commaIdx + 1;
            }
        }
        return new FieldSelector(fields, nested);
    }

    /**
     * Checks whether the specified field should be included in serialization.
     * <p>
     * A field is included if:
     * <ul>
     *   <li>The selector is in unrestricted mode (fields is null), or</li>
     *   <li>The field is explicitly listed in the fields set, or</li>
     *   <li>The field has a nested selector defined</li>
     * </ul>
     * </p>
     *
     * @param field the name of the field to check
     * @return true if the field should be included, false otherwise
     */
    public boolean includes(String field) {
        if (fields == null) return true;
        if (fields.contains(field)) return true;
        return nested != null && nested.containsKey(field);
    }

    /**
     * Checks whether this selector is in unrestricted mode, allowing all fields.
     * <p>
     * Unrestricted mode occurs when no field specification was provided during parsing
     * (i.e., the specification was null or blank).
     * </p>
     *
     * @return true if all fields should be included without restrictions, false otherwise
     */
    public boolean includeAll() {
        return fields == null;
    }

    /**
     * Retrieves the nested FieldSelector for the specified field.
     * <p>
     * If no nested selector is defined for the field, returns an unrestricted selector
     * that allows all fields at the nested level.
     * </p>
     *
     * @param field the name of the field whose nested selector to retrieve
     * @return the nested FieldSelector for the field, or an unrestricted selector if none exists
     */
    public FieldSelector nested(String field) {
        if (nested == null || !nested.containsKey(field)) {
            return new FieldSelector(null, null); // no restriction on nested level either
        }
        return nested.get(field);
    }

    /**
     * Finds the index of the next comma at the current nesting depth, or the end of the string.
     * <p>
     * This method tracks parenthesis depth to avoid splitting on commas that are inside
     * nested field specifications. Only commas at depth 0 (not inside any parentheses)
     * are considered as field separators.
     * </p>
     *
     * @param spec the specification string to search
     * @param from the starting index for the search
     * @return the index of the next comma at depth 0, or the length of the string if none found
     */
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

    /**
     * Finds the index of the closing parenthesis that matches the opening parenthesis at the specified position.
     * <p>
     * This method correctly handles nested parentheses by tracking the depth level and returning
     * the index of the closing parenthesis that brings the depth back to 0.
     * </p>
     *
     * @param spec    the specification string to search
     * @param openIdx the index of the opening parenthesis
     * @return the index of the matching closing parenthesis
     * @throws IllegalArgumentException if no matching closing parenthesis is found
     */
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
