package com.netgrif.application.engine.elastic;

import com.netgrif.application.engine.elastic.service.ElasticsearchQuerySanitizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ElasticsearchQuerySanitizerTest {

    @Test
    void shouldSanitizeQuery() {
        String query = "identifier: some_value AND field.keyword.value <> other_value";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertNotNull(sanitized);
        assertEquals("identifier\\:\\ some_value\\ \\A\\N\\D\\ field.keyword.value\\ \\ \\ " +
                "\\ other_value", sanitized);
    }

    @Test
    void shouldEscapeReservedCharacters() {
        String query = "test\\query";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("test\\\\query", sanitized);
    }

    @Test
    void shouldEscapeSpecialOperators() {
        String query = "field: value + other - something = test";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("field\\:\\ value\\ \\+\\ other\\ \\-\\ something\\ \\=\\ test", sanitized);
    }

    @Test
    void shouldEscapeLogicalOperators() {
        String query = "field1 && field2 || field3";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("field1\\ \\&\\&\\ field2\\ \\|\\|\\ field3", sanitized);
    }

    @Test
    void shouldEscapeBracketsAndParentheses() {
        String query = "field: (value) {range} [array]";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("field\\:\\ \\(value\\)\\ \\{range\\}\\ \\[array\\]", sanitized);
    }

    @Test
    void shouldEscapeWildcardsAndSpecialChars() {
        String query = "test*query?with^special\"chars~";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("test\\*query\\?with\\^special\\\"chars\\~", sanitized);
    }

    @Test
    void shouldRemoveAngleBrackets() {
        String query = "value<100 AND value>50";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("value\\ 100\\ \\A\\N\\D\\ value\\ 50", sanitized);
    }

    @Test
    void shouldEscapeSlashes() {
        String query = "path/to/resource";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("path\\/to\\/resource", sanitized);
    }

    @Test
    void shouldEscapeNegationOperator() {
        String query = "!important";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("\\!important", sanitized);
    }

    @Test
    void shouldEscapeKeywordsAndOrNot() {
        String query = "field1 AND field2 OR field3 NOT field4";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("field1\\ \\A\\N\\D\\ field2\\ \\O\\R\\ field3\\ \\N\\O\\T\\ field4", sanitized);
    }

    @Test
    void shouldHandleNullQuery() {
        String sanitized = ElasticsearchQuerySanitizer.sanitize(null);
        assertNull(sanitized);
    }

    @Test
    void shouldHandleEmptyQuery() {
        String sanitized = ElasticsearchQuerySanitizer.sanitize("");
        assertEquals("", sanitized);
    }

    @Test
    void shouldHandleBlankQuery() {
        String sanitized = ElasticsearchQuerySanitizer.sanitize("   ");
        assertEquals("   ", sanitized);
    }

    @Test
    void shouldHandleMultipleReservedCharactersInSequence() {
        String query = "field:value&&another||test";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("field\\:value\\&\\&another\\|\\|test", sanitized);
    }

    @Test
    void shouldSanitizeWithExcludedKeywords() {
        String query = "field:value AND other:test";
        String[] exclude = {"AND"," "};
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query, exclude);
        assertEquals("field\\:value AND other\\:test", sanitized);
    }

    @Test
    void shouldSanitizeWithMultipleExcludedKeywords() {
        String query = "field:value AND other:test OR another:value";
        String[] exclude = {"AND", "OR", " "};
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query, exclude);
        assertEquals("field\\:value AND other\\:test OR another\\:value", sanitized);
    }

    @Test
    void shouldSanitizeWithExcludedSpecialCharacters() {
        String query = "field:value + test - other";
        String[] exclude = {"+", "-", " "};
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query, exclude);
        assertEquals("field\\:value + test - other", sanitized);
    }

    @Test
    void shouldHandleNullExcludeArray() {
        String query = "field: value AND test";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query, null);
        assertEquals("field\\:\\ value\\ \\A\\N\\D\\ test", sanitized);
    }

    @Test
    void shouldHandleEmptyExcludeArray() {
        String query = "field: value AND test";
        String[] exclude = {};
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query, exclude);
        assertEquals("field\\:\\ value\\ \\A\\N\\D\\ test", sanitized);
    }

    @Test
    void shouldIgnoreNonReservedKeywordsInExclude() {
        String query = "field: value AND test";
        String[] exclude = {"NONEXISTENT", "INVALID"};
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query, exclude);
        assertEquals("field\\:\\ value\\ \\A\\N\\D\\ test", sanitized);
    }

    @Test
    void shouldHandleComplexQueryWithMixedCharacters() {
        String query = "title:(\"test query\" AND status:active) OR tags:[java, spring] && created_at:[2023 TO 2024]";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertNotNull(sanitized);
        assertTrue(sanitized.contains("\\:"));
        assertTrue(sanitized.contains("\\("));
        assertTrue(sanitized.contains("\\)"));
        assertTrue(sanitized.contains("\\["));
        assertTrue(sanitized.contains("\\]"));
    }

    @Test
    void shouldEscapeSpaces() {
        String query = "hello world";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("hello\\ world", sanitized);
    }

    @Test
    void shouldHandleQueryWithOnlyReservedCharacters() {
        String query = "+-=!(){}[]^\"~*?:/";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertNotNull(sanitized);
        assertNotEquals(query, sanitized);
    }

    @Test
    void shouldNotModifyQueryWithoutReservedCharacters() {
        String query = "simple search query";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        // Note: space is a reserved character, so this will be escaped
        assertEquals("simple\\ search\\ query", sanitized);
    }

    @Test
    void shouldNotModifyQueryWithoutReservedCharactersExcludingSpace() {
        String query = "simple search query";
        String[] exclude = {" "};
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query, exclude);
        // Note: space is a reserved character, so this will be escaped
        assertEquals("simple search query", sanitized);
    }

    @Test
    void shouldNotModifyQueryWithoutAnyReservedCharacters() {
        String query = "simplesearchquery";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        assertEquals("simplesearchquery", sanitized);
    }

}
