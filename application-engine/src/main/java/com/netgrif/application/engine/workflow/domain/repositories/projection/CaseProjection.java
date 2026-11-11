package com.netgrif.application.engine.workflow.domain.repositories.projection;

import java.util.*;

/**
 * Describes which {@code dataSet} entries of a {@code Case} should be loaded by projected (partial) read queries.
 * All static (non-{@code dataSet}) fields are always loaded.
 *
 * @param dataSetKeys immutable set of requested data field ids
 */
public record CaseProjection(Set<String> dataSetKeys) {

    public CaseProjection {
        dataSetKeys = dataSetKeys == null ? Set.of() : Set.copyOf(dataSetKeys);
    }

    /**
     * Creates a projection from the given keys. Nulls are dropped, keys are trimmed, blanks removed and duplicates
     * eliminated (insertion order is retained during normalization).
     *
     * @param keys requested data field ids, may be {@code null}
     * @return the projection
     */
    public static CaseProjection ofDataFields(Collection<String> keys) {
        return new CaseProjection(normalize(keys));
    }

    /**
     * @param keys additional data field ids, normalized in the same way as in {@link #ofDataFields(Collection)}
     * @return a new projection containing keys of this projection and the additional ones
     */
    public CaseProjection withAdditionalKeys(Collection<String> keys) {
        Set<String> merged = new LinkedHashSet<>(dataSetKeys);
        merged.addAll(normalize(keys));
        return new CaseProjection(merged);
    }

    private static Set<String> normalize(Collection<String> keys) {
        if (keys == null) {
            return new LinkedHashSet<>();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            String trimmed = key.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
