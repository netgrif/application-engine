package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.repositories.projection.CaseProjection;
import com.querydsl.core.types.Predicate;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Read-only projected queries for {@link Case}. Returned cases contain all static fields but only the requested
 * {@code dataSet} entries. They are partial and READ-ONLY: they must never be persisted with {@code save(...)}.
 */
public interface CaseProjectionRepository {

    /**
     * Loads a PARTIAL, READ-ONLY case: all static fields plus only the {@code dataSet} entries in the projection.
     * The result must never be saved.
     *
     * @param objectId   object id part of the composite case id
     * @param projection requested data set keys
     * @return the partially loaded case, if found
     * @throws IllegalArgumentException if any key is invalid
     */
    Optional<Case> findProjectedByObjectId(ObjectId objectId, CaseProjection projection);

    /**
     * Loads PARTIAL, READ-ONLY cases with a single query. Order is not guaranteed. The results must never be saved.
     *
     * @param objectIds  object id parts of the composite case ids
     * @param projection requested data set keys
     * @return partially loaded cases; empty list (without querying) for empty input
     * @throws IllegalArgumentException if any key is invalid
     */
    List<Case> findAllProjectedByObjectIds(Collection<ObjectId> objectIds, CaseProjection projection);

    /**
     * Persists a PARTIALLY LOADED case using Mongo {@code $set} update operations instead of replacing the document.
     * Only the static fields (except {@code _id}) and the {@code dataSet} entries that are present in the given case
     * are written, so {@code dataSet} entries that were never loaded are left untouched. {@code lastModified} is
     * refreshed.
     *
     * @param partial a case obtained from a projected read
     * @return {@code true} if a document was matched
     * @throws IllegalArgumentException if any present data set key is invalid
     */
    boolean updatePartial(Case partial);

    /**
     * Loads a page of PARTIAL, READ-ONLY cases (all static fields plus the projected {@code dataSet} entries).
     * Sorting and paging are applied by Mongo. The results must never be saved.
     *
     * @param pageable   paging and sorting
     * @param projection requested data set keys
     * @return page of partially loaded cases
     * @throws IllegalArgumentException if any key is invalid
     */
    Page<Case> findAllProjected(Pageable pageable, CaseProjection projection);

    /**
     * Loads a PARTIAL, READ-ONLY case matching the given predicate: all static fields plus only the {@code dataSet}
     * entries in the projection. The result must never be saved.
     *
     * @param predicate  Querydsl predicate
     * @param projection requested data set keys
     * @return the partially loaded case, if found
     * @throws IllegalArgumentException if any key is invalid
     */
    Optional<Case> findOneProjected(Predicate predicate, CaseProjection projection);

    /**
     * Loads PARTIAL, READ-ONLY cases matching the given predicate: all static fields plus only the {@code dataSet}
     * entries in the projection. The results must never be saved.
     *
     * @param predicate  Querydsl predicate
     * @param projection requested data set keys
     * @return partially loaded cases
     * @throws IllegalArgumentException if any key is invalid
     */
    List<Case> findAllProjected(Predicate predicate, CaseProjection projection);

    /**
     * Loads a page of PARTIAL, READ-ONLY cases matching the given predicate (all static fields plus the projected
     * {@code dataSet} entries). Sorting and paging are applied by Mongo. The results must never be saved.
     *
     * @param predicate  Querydsl predicate
     * @param pageable   paging and sorting
     * @param projection requested data set keys
     * @return page of partially loaded cases
     * @throws IllegalArgumentException if any key is invalid
     */
    Page<Case> findAllProjected(Predicate predicate, Pageable pageable, CaseProjection projection);
}
