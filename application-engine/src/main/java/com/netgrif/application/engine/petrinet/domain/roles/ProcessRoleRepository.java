package com.netgrif.application.engine.petrinet.domain.roles;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository interface for managing {@link ProcessRole} entities in a MongoDB database.
 * Extends {@link MongoRepository} for standard CRUD operations and {@link QuerydslPredicateExecutor}
 * to support dynamic queries.
 */
@Repository
public interface ProcessRoleRepository extends MongoRepository<ProcessRole, String>, QuerydslPredicateExecutor<ProcessRole> {

    /**
     * Finds a {@link ProcessRole} by its import ID.
     *
     * @param importId the import ID to search for
     * @return an {@link Optional} containing the found {@link ProcessRole}, if any
     */
    Optional<ProcessRole> findByImportId(String importId);

    /**
     * Finds a paginated list of all {@link ProcessRole} entities associated with a specific process ID.
     *
     * @param netId    the process ID
     * @param pageable the pageable object for pagination settings
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByProcessId(String netId, Pageable pageable);

    /**
     * Finds a paginated list of all {@link ProcessRole} entities by their default name value.
     *
     * @param name     the default name value
     * @param pageable the pageable object for pagination settings
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByName_DefaultValue(String name, Pageable pageable);

    /**
     * Finds a paginated list of all {@link ProcessRole} entities by their import ID with pagination.
     *
     * @param importId the import ID to filter
     * @param pageable the pageable object for pagination settings
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByImportId(String importId, Pageable pageable);

    /**
     * Finds a paginated list of all global {@link ProcessRole} entities where the global flag is set to true.
     *
     * @param pageable the pageable object for pagination settings
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByGlobalIsTrue(Pageable pageable);

    /**
     * Finds a {@link ProcessRole} by its object ID.
     *
     * @param objectId the object ID to search for
     * @return an {@link Optional} containing the found {@link ProcessRole}, if any
     */
    @Query("{ '_id.objectId': ?0 }")
    Optional<ProcessRole> findByIdObjectId(ObjectId objectId);

    /**
     * todo javadoc
     */
    @Query("{ '_id.objectId': ?0 }")
    void deleteByObjectId(ObjectId objectId);

    /**
     * Finds all {@link ProcessRole} entities by their object IDs.
     *
     * @param objectIds a {@link Collection} of object IDs
     * @return a {@link List} of {@link ProcessRole} entities
     */
    @Query("{ '_id.objectId': { $in: ?0 } }")
    List<ProcessRole> findByObjectIds(Collection<ObjectId> objectIds);

    /**
     * Deletes all {@link ProcessRole} entities with the provided collection of resource IDs.
     *
     * @param ids a collection of {@link ProcessResourceId} entities
     */
    void deleteAllBy_idIn(Collection<ProcessResourceId> ids);

    /**
     * Finds all {@link ProcessRole} entities by a collection of IDs, where the IDs may
     * represent either object IDs or composite IDs (containing a network ID and an object ID).
     *
     * @param ids a collection of process role IDs to filter
     * @return a {@link Set} of {@link ProcessRole} entities
     */
    default Set<ProcessRole> findAllByIdsSet(Collection<String> ids) {
        Map<Boolean, List<String>> partitionedIds = ids.stream()
                .collect(Collectors.partitioningBy(id -> id.contains(ProcessResourceId.ID_SEPARATOR)));

        List<ObjectId> forObjectIds = partitionedIds.get(false).stream()
                .map(ObjectId::new)
                .toList();

        List<ProcessResourceId> processResourceIds = partitionedIds.get(true).stream()
                .map(ProcessResourceId::new)
                .toList();

        List<ProcessRole> processRoles = new ArrayList<>();
        processRoles.addAll(findByObjectIds(forObjectIds));
        processRoles.addAll(findByProcessResourceIds(processResourceIds));
        return new HashSet<>(processRoles);
    }

    /**
     * Finds a {@link ProcessRole} by its composite ID. The composite ID may represent either a single
     * object ID or a combination of network ID and object ID.
     *
     * @param compositeId the composite ID to search for
     * @return an {@link Optional} containing the found {@link ProcessRole}, if any
     */
    default Optional<ProcessRole> findByCompositeId(String compositeId) {
        String[] parts = compositeId.split(ProcessResourceId.ID_SEPARATOR);
        if (parts.length == 2) {
            String networkId = parts[0];
            ObjectId objectId = new ObjectId(parts[1]);
            return findByNetworkIdAndObjectId(networkId, objectId);
        } else {
            return findByIdObjectId(new ObjectId(compositeId));
        }
    }

    /**
     * todo javadoc
     */
    default void deleteByCompositeId(String compositeId) {
        String[] parts = compositeId.split(ProcessResourceId.ID_SEPARATOR);
        if (parts.length == 2) {
            String networkId = parts[0];
            ObjectId objectId = new ObjectId(parts[1]);
            deleteByNetworkIdAndObjectId(networkId, objectId);
        } else {
            deleteByObjectId(new ObjectId(compositeId));
        }
    }

    /**
     * Finds a {@link ProcessRole} by a network ID and object ID.
     *
     * @param networkId the short process ID
     * @param objectId  the object ID
     * @return an {@link Optional} containing the found {@link ProcessRole}, if any
     */
    @Query("{ '_id.shortProcessId': ?0, '_id.objectId': ?1 }")
    Optional<ProcessRole> findByNetworkIdAndObjectId(String networkId, ObjectId objectId);

    /**
     * todo javadoc
     */
    @Query("{ '_id.shortProcessId': ?0, '_id.objectId': ?1 }")
    void deleteByNetworkIdAndObjectId(String networkId, ObjectId objectId);

    /**
     * Finds all {@link ProcessRole} entities by a collection of composite resource IDs.
     *
     * @param compositeIds a collection of {@link ProcessResourceId} instances representing the composite IDs to filter
     * @return a {@link List} of {@link ProcessRole} entities matching the provided composite IDs
     */
    @Query("{ '_id': { $in: ?0 } }")
    List<ProcessRole> findByProcessResourceIds(Collection<ProcessResourceId> compositeIds);
}