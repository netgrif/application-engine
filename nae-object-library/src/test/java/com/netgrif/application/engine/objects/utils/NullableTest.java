package com.netgrif.application.engine.objects.utils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NullableTest {

    @Test
    void createsPresentAndEmptyValuesWithOptionalTypeInformation() {
        Nullable<String> present = Nullable.of("value", String.class);
        Nullable<String> empty = Nullable.empty(String.class);

        assertEquals("value", present.get());
        assertEquals(String.class, present.getType());
        assertTrue(present.isPresent());
        assertFalse(present.isEmpty());
        assertEquals(String.class, empty.getType());
        assertFalse(empty.isPresent());
        assertTrue(empty.isEmpty());
    }

    @Test
    void runsPresentOrEmptyCallbacks() {
        AtomicReference<String> seen = new AtomicReference<>();
        AtomicBoolean emptyCallbackCalled = new AtomicBoolean(false);

        Nullable.of("value").ifPresent(seen::set);
        Nullable.<String>empty().ifPresentOrElse(seen::set, () -> emptyCallbackCalled.set(true));

        assertEquals("value", seen.get());
        assertTrue(emptyCallbackCalled.get());
    }

    @Test
    void convertsToOptionalAndStream() {
        assertEquals(Optional.of("value"), Nullable.of("value").toOptional());
        assertEquals(Optional.empty(), Nullable.empty().toOptional());
        assertEquals(List.of("value"), Nullable.of("value").stream().toList());
        assertTrue(Nullable.empty().stream().toList().isEmpty());
    }

    @Test
    void filtersPresentValuesAndPreservesTypeOnFilteredEmpty() {
        Nullable<String> present = Nullable.of("value", String.class);

        assertSame(present, present.filter(value -> value.startsWith("v")));
        Nullable<String> filtered = present.filter(value -> value.startsWith("x"));

        assertTrue(filtered.isEmpty());
        assertEquals(String.class, filtered.getType());
        assertSame(filtered, filtered.filter(value -> true));
    }

    @Test
    void mapsAndFlatMapsPresentValues() {
        Nullable<Integer> mapped = Nullable.of("value").map(String::length);
        Nullable<Integer> flatMapped = Nullable.of("value").flatMap(value -> Nullable.of(value.length()));

        assertEquals(5, mapped.get());
        assertEquals(5, flatMapped.get());
        assertTrue(Nullable.<String>empty().map(String::length).isEmpty());
        assertTrue(Nullable.<String>empty().flatMap(value -> Nullable.of(value.length())).isEmpty());
    }

    @Test
    void returnsAlternativeNullableOnlyWhenEmpty() {
        Nullable<String> present = Nullable.of("value");
        Nullable<String> alternative = Nullable.of("fallback");

        assertSame(present, present.or(() -> alternative));
        assertSame(alternative, Nullable.<String>empty().or(() -> alternative));
    }

    @Test
    void resolvesFallbackValuesAndThrowsWhenEmpty() {
        assertEquals("value", Nullable.of("value").orElse("fallback"));
        assertEquals("fallback", Nullable.<String>empty().orElse("fallback"));
        assertEquals("value", Nullable.of("value").orElseGet(() -> "fallback"));
        assertEquals("fallback", Nullable.<String>empty().orElseGet(() -> "fallback"));
        assertEquals("value", Nullable.of("value").orElseThrow());

        assertThrows(NoSuchElementException.class, () -> Nullable.empty().orElseThrow());
        assertThrows(IllegalStateException.class, () ->
                Nullable.empty().orElseThrow(() -> new IllegalStateException("missing"))
        );
    }

    @Test
    void comparesValueAndTypeAndUsesValueAsStringRepresentation() {
        Nullable<String> first = Nullable.of("value", String.class);
        Nullable<String> same = Nullable.of("value", String.class);
        Nullable<String> differentType = Nullable.of("value");
        Nullable<String> empty = Nullable.empty();

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, differentType);
        assertEquals("value", first.toString());
        assertEquals("", empty.toString());
    }
}
