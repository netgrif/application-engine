package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.utils.MongodbUtils;
import com.netgrif.application.engine.workflow.domain.repositories.projection.CaseProjection;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import java.time.LocalDateTime;

import java.util.*;
import java.util.regex.Pattern;


/**
 * Implementation of {@link CaseProjectionRepository} providing MongoDB-specific query execution
 * with field projections for {@link Case} entities.
 * <p>
 * This repository optimizes performance and payload size by loading partial case representations.
 * Query results include all static entity properties, but only the subset of {@code dataSet}
 * entries explicitly requested via {@link CaseProjection}.
 * </p>
 * <p>
 * All case entities returned by query methods are marked as partial (read-only) and must not be
 * persisted using standard full-document save methods, as doing so would overwrite and clear
 * non-projected {@code dataSet} fields. Updates to partial cases must be performed exclusively via
 * {@link #updatePartial(Case)}.
 * </p>
 */
@RequiredArgsConstructor
class CaseProjectionRepositoryImpl implements CaseProjectionRepository {

    /**
     * Name of the dynamic data set map field in the MongoDB document.
     */
    private static final String DATA_SET_FIELD = "dataSet";

    /**
     * Spring Data MongoDB template used for low-level query execution and update operations.
     */
    private final MongoTemplate mongoTemplate;

    /**
     * MongoDB mapping context used for metadata inspection and entity property reflection.
     */
    private final MongoMappingContext mappingContext;

    /**
     * Lazily initialized cached list of static property names for the concrete Case entity class.
     */
    private volatile List<String> staticFields;

    /**
     * Loads a PARTIAL, READ-ONLY case by its underlying ObjectId.
     *
     * @param objectId   object id part of the composite case id
     * @param projection requested data set keys to include in the projection
     * @return an {@link Optional} containing the partially loaded case if found, or empty otherwise
     * @throws IllegalArgumentException if any data set key in the projection is invalid
     */
    @Override
    public Optional<Case> findProjectedByObjectId(ObjectId objectId, CaseProjection projection) {
        Query query = new Query(Criteria.where("_id.objectId").is(objectId));
        applyProjection(query, projection);
        return Optional.ofNullable(mongoTemplate.findOne(query, concreteCaseClass()))
                .map(CaseProjectionRepositoryImpl::markPartial);
    }

    /**
     * Loads PARTIAL, READ-ONLY cases with a single query for a collection of ObjectIds.
     *
     * @param objectIds  collection of object id parts of composite case ids
     * @param projection requested data set keys to include in the projection
     * @return list of partially loaded cases, or an empty list if {@code objectIds} is null or empty
     * @throws IllegalArgumentException if any data set key in the projection is invalid
     */
    @Override
    public List<Case> findAllProjectedByObjectIds(Collection<ObjectId> objectIds, CaseProjection projection) {
        if (objectIds == null || objectIds.isEmpty()) {
            return new ArrayList<>();
        }
        Query query = new Query(Criteria.where("_id.objectId").in(objectIds));
        applyProjection(query, projection);
        List<Case> result = new ArrayList<>();
        for (Case c : mongoTemplate.find(query, concreteCaseClass())) {
            result.add(markPartial(c));
        }
        return result;
    }

    /**
     * Loads a page of PARTIAL, READ-ONLY cases matching pagination and sorting criteria.
     *
     * @param pageable   pagination and sorting information
     * @param projection requested data set keys to include in the projection
     * @return a {@link Page} of partially loaded cases along with total count metadata
     * @throws IllegalArgumentException if any data set key in the projection is invalid
     */
    @Override
    public Page<Case> findAllProjected(Pageable pageable, CaseProjection projection) {
        Query query = new Query();
        applyProjection(query, projection);
        if (pageable != null && pageable.isPaged()) {
            query.with(pageable);
        } else if (pageable != null) {
            query.with(pageable.getSort());
        }
        List<Case> content = new ArrayList<>();
        for (Case c : mongoTemplate.find(query, concreteCaseClass())) {
            content.add(markPartial(c));
        }
        return PageableExecutionUtils.getPage(content, pageable == null ? Pageable.unpaged() : pageable,
                () -> mongoTemplate.count(new Query(), concreteCaseClass()));
    }

    /**
     * Loads a single PARTIAL, READ-ONLY case matching the given Querydsl predicate.
     *
     * @param predicate  Querydsl predicate defining filter criteria
     * @param projection requested data set keys to include in the projection
     * @return an {@link Optional} containing the partially loaded case if found, or empty otherwise
     * @throws IllegalArgumentException if any data set key in the projection is invalid
     */
    @Override
    public Optional<Case> findOneProjected(Predicate predicate, CaseProjection projection) {
        Query query = predicate == null ? new Query() : MongodbUtils.toQuery(mongoTemplate, concreteCaseClass(), predicate);
        applyProjection(query, projection);
        return Optional.ofNullable(mongoTemplate.findOne(query, concreteCaseClass()))
                .map(CaseProjectionRepositoryImpl::markPartial);
    }

    /**
     * Loads all PARTIAL, READ-ONLY cases matching the given Querydsl predicate.
     *
     * @param predicate  Querydsl predicate defining filter criteria
     * @param projection requested data set keys to include in the projection
     * @return list of partially loaded cases matching the predicate
     * @throws IllegalArgumentException if any data set key in the projection is invalid
     */
    @Override
    public List<Case> findAllProjected(Predicate predicate, CaseProjection projection) {
        Query query = predicate == null ? new Query() : MongodbUtils.toQuery(mongoTemplate, concreteCaseClass(), predicate);
        applyProjection(query, projection);
        List<Case> result = new ArrayList<>();
        for (Case c : mongoTemplate.find(query, concreteCaseClass())) {
            result.add(markPartial(c));
        }
        return result;
    }

    /**
     * Loads a page of PARTIAL, READ-ONLY cases matching the given Querydsl predicate and pagination criteria.
     *
     * @param predicate  Querydsl predicate defining filter criteria
     * @param pageable   pagination and sorting information
     * @param projection requested data set keys to include in the projection
     * @return a {@link Page} of partially loaded cases along with total count matching the predicate
     * @throws IllegalArgumentException if any data set key in the projection is invalid
     */
    @Override
    public Page<Case> findAllProjected(Predicate predicate, Pageable pageable, CaseProjection projection) {
        Query query = predicate == null ? new Query() : MongodbUtils.toQuery(mongoTemplate, concreteCaseClass(), predicate);
        applyProjection(query, projection);
        if (pageable != null && pageable.isPaged()) {
            query.with(pageable);
        } else if (pageable != null) {
            query.with(pageable.getSort());
        }
        List<Case> content = new ArrayList<>();
        for (Case c : mongoTemplate.find(query, concreteCaseClass())) {
            content.add(markPartial(c));
        }
        Query countQuery = predicate == null ? new Query() : MongodbUtils.toQuery(mongoTemplate, concreteCaseClass(), predicate);
        return PageableExecutionUtils.getPage(content, pageable == null ? Pageable.unpaged() : pageable,
                () -> mongoTemplate.count(countQuery, concreteCaseClass()));
    }

    /**
     * Persists a PARTIAL case via MongoDB {@code $set} operations without overwriting non-projected dataSet entries.
     * <p>
     * Updates all static entity properties (excluding the {@code _id} property) and sets only the {@code dataSet}
     * keys present in the provided partial case. The {@code lastModified} timestamp is updated to the current time.
     * </p>
     *
     * @param partial partially loaded case to update
     * @return {@code true} if an existing case document matched and was updated, {@code false} otherwise
     * @throws IllegalArgumentException if any present dataSet key is invalid (e.g., contains '$', '.', or null bytes)
     */
    @Override
    public boolean updatePartial(Case partial) {
        Map<String, com.netgrif.application.engine.objects.workflow.domain.DataField> dataSet = partial.getDataSet();
        Set<String> keys = dataSet == null ? Set.of() : dataSet.keySet();
        for (String key : keys) {
            validateKey(key);
        }
        partial.setLastModified(LocalDateTime.now());
        Update update = new Update();
        PersistentPropertyAccessor<Case> accessor = mappingContext.getRequiredPersistentEntity(concreteCaseClass()).getPropertyAccessor(partial);
        mappingContext.getRequiredPersistentEntity(concreteCaseClass()).doWithProperties((PropertyHandler<MongoPersistentProperty>) property -> {
            String name = property.getFieldName();
            if (property.isIdProperty() || DATA_SET_FIELD.equals(name)) {
                return;
            }
            update.set(name, accessor.getProperty(property));
        });
        for (String key : keys) {
            update.set(DATA_SET_FIELD + "." + key, dataSet.get(key));
        }
        Query query = new Query(Criteria.where("_id.objectId").is(partial.get_id().getObjectId()));
        return mongoTemplate.updateFirst(query, update, concreteCaseClass()).getMatchedCount() > 0;
    }

    /**
     * Marks the case instance as partially loaded to prevent accidental full-document persistence.
     *
     * @param c case instance to mark
     * @return the same case instance with {@code partiallyLoaded} set to {@code true}
     */
    private static Case markPartial(Case c) {
        c.setPartiallyLoaded(true);
        return c;
    }

    /**
     * Returns the concrete Spring Data / MongoDB mapped Case entity class.
     *
     * @return concrete Case class definition
     */
    private static Class<? extends Case> concreteCaseClass() {
        return com.netgrif.application.engine.adapter.spring.workflow.domain.Case.class;
    }

    /**
     * Applies projection inclusions to the provided MongoDB query.
     * Involves all static document fields and the specified subset of {@code dataSet} fields.
     *
     * @param query      MongoDB query to modify
     * @param projection projection specifications and data set keys to include
     * @throws IllegalArgumentException if any data set key is invalid
     */
    private void applyProjection(Query query, CaseProjection projection) {
        Set<String> keys = projection == null ? Set.of() : projection.dataSetKeys();
        for (String key : keys) {
            validateKey(key);
        }
        for (String field : getStaticFields()) {
            query.fields().include(field);
        }
        for (String key : keys) {
            query.fields().include(DATA_SET_FIELD + "." + key);
        }
    }

    /**
     * Validates that a dataSet key is well-formed and safe for MongoDB field path operations.
     *
     * @param key key string to validate
     * @throws IllegalArgumentException if the key is null, blank, or contains reserved characters ('$', '.', null byte)
     */
    private static void validateKey(String key) {
        if (key == null || key.isBlank() || key.contains("$") || key.contains(".") || key.contains("\0")) {
            throw new IllegalArgumentException("Invalid dataSet key [" + key + "]");
        }
    }

    /**
     * Retrieves the cached list of all static property names of the concrete Case entity,
     * excluding the dynamic {@code dataSet} field. Uses double-checked locking for thread-safe lazy loading.
     *
     * @return list of static property names
     */
    private List<String> getStaticFields() {
        List<String> local = staticFields;
        if (local == null) {
            synchronized (this) {
                local = staticFields;
                if (local == null) {
                    MongoPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(concreteCaseClass());
                    List<String> names = new ArrayList<>();
                    entity.doWithProperties((PropertyHandler<MongoPersistentProperty>) property -> {
                        if (!DATA_SET_FIELD.equals(property.getFieldName())) {
                            names.add(property.getFieldName());
                        }
                    });
                    local = List.copyOf(names);
                    staticFields = local;
                }
            }
        }
        return local;
    }
}
