package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.workflow.domain.repositories.projection.CaseProjection;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CaseProjectionTest {

    @Test
    void ofDataFieldsDropsNullsBlanksAndDuplicates() {
        CaseProjection projection = CaseProjection.ofDataFields(Arrays.asList(" a ", null, "", "  ", "a", "b"));
        assertEquals(Set.of("a", "b"), projection.dataSetKeys());
    }

    @Test
    void nullInputIsNullSafe() {
        assertTrue(CaseProjection.ofDataFields(null).dataSetKeys().isEmpty());
        assertTrue(new CaseProjection(null).dataSetKeys().isEmpty());
    }

    @Test
    void keysAreImmutableAndDefensivelyCopied() {
        List<String> source = new java.util.ArrayList<>(List.of("a"));
        CaseProjection projection = CaseProjection.ofDataFields(source);
        source.add("b");
        assertEquals(Set.of("a"), projection.dataSetKeys());
        assertThrows(UnsupportedOperationException.class, () -> projection.dataSetKeys().add("c"));
    }

    @Test
    void withAdditionalKeysReturnsNewMergedProjection() {
        CaseProjection base = CaseProjection.ofDataFields(List.of("a"));
        CaseProjection merged = base.withAdditionalKeys(Arrays.asList("b", null, " a "));
        assertEquals(Set.of("a"), base.dataSetKeys());
        assertEquals(Set.of("a", "b"), merged.dataSetKeys());
    }
}
