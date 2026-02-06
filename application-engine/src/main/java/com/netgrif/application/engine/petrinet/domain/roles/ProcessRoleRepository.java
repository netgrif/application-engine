package com.netgrif.application.engine.petrinet.domain.roles;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.*;

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
     * Finds a {@link ProcessRole} by its import ID and workspace ID.
     *
     * @param importId the import ID to search for
     * @param workspaceId the ID of the workspace
     * @return an {@link Optional} containing the found {@link ProcessRole}, if any
     */
    Optional<ProcessRole> findByImportIdAndWorkspaceId(String importId, String workspaceId);

    /**
     * Finds a paginated list of all {@link ProcessRole} entities associated with a specific process ID.
     *
     * @param netId    the process ID
     * @param pageable the pageable object for pagination settings
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByProcessId(String netId, Pageable pageable);

    // todo javadoc
    Page<ProcessRole> findAllByWorkspaceId(String workspaceId, Pageable pageable);

    /**
     * Finds a paginated list of all {@link ProcessRole} entities by their default name value.
     *
     * @param name     the default name value
     * @param pageable the pageable object for pagination settings
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByName_DefaultValue(String name, Pageable pageable);

    /**
     * Finds a paginated list of all {@link ProcessRole} entities by their default name value.
     *
     * @param name     the default name value
     * @param workspaceId the ID of the workspace
     * @param pageable the pageable object for pagination settings
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByName_DefaultValueAndWorkspaceId(String name, String workspaceId, Pageable pageable);

    /**
     * Finds a paginated list of all {@link ProcessRole} entities by their import ID with pagination.
     *
     * @param importId the import ID to filter
     * @param pageable the pageable object for pagination settings
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByImportId(String importId, Pageable pageable);

    /**
     * Finds a paginated list of all {@link ProcessRole} entities by their import ID and workspace ID with pagination.
     *
     * @param importId the import ID to filter
     * @param workspaceId the ID of the workspace
     * @param pageable the pageable object for pagination settings
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByImportIdAndWorkspaceId(String importId, String workspaceId, Pageable pageable);

    /**
     * Finds a paginated list of all global {@link ProcessRole} entities where the global flag is set to true.
     *
     * @param pageable the pageable object for pagination settings
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByGlobalIsTrue(Pageable pageable);

    /**
     * Finds a paginated list of all global {@link ProcessRole} entities where the global flag is set to true and by workspace ID.
     *
     * @param pageable the pageable object for pagination settings
     * @param workspaceId the ID of the workspace
     * @return a {@link Page} of {@link ProcessRole} entities
     */
    Page<ProcessRole> findAllByGlobalIsTrueAndWorkspaceId(String workspaceId, Pageable pageable);

    /**
     * Finds a {@link ProcessRole} by its object ID.
     *
     * @param objectId the object ID to search for
     * @return an {@link Optional} containing the found {@link ProcessRole}, if any
     */
    @Query("{ '_id.objectId': ?0 }")
    Optional<ProcessRole> findByIdObjectId(ObjectId objectId);

    /**
     * Finds a {@link ProcessRole} by its object ID and workspace ID.
     *
     * @param objectId the object ID to search for
     * @param workspaceId the ID of the workspace
     * @return an {@link Optional} containing the found {@link ProcessRole}, if any
     */
    @Query("{ '_id.objectId': ?0, 'workspaceId': ?1 }")
    Optional<ProcessRole> findByIdObjectIdAndWorkspaceId(ObjectId objectId, String workspaceId);

    /**
     * Finds all {@link ProcessRole} entities by their object IDs.
     *
     * @param objectIds a {@link Collection} of object IDs
     * @return a {@link List} of {@link ProcessRole} entities
     */
    @Query("{ '_id.objectId': { $in: ?0 } }")
    List<ProcessRole> findByObjectIds(Collection<ObjectId> objectIds);

    /**
     * Finds all {@link ProcessRole} entities by their object IDs and workspace ID.
     *
     * @param objectIds a {@link Collection} of object IDs
     * @param workspaceId the ID of the workspace
     * @return a {@link List} of {@link ProcessRole} entities
     */
    @Query("{ '_id.objectId': { $in: ?0 }, 'workspaceId': ?1 }")
    List<ProcessRole> findByObjectIdsAndWorkspaceId(Collection<ObjectId> objectIds, String workspaceId);

    /**
     * Deletes all {@link ProcessRole} entities with the provided collection of resource IDs.
     *
     * @param ids a collection of {@link ProcessResourceId} entities
     */
    void deleteAllBy_idIn(Collection<ProcessResourceId> ids);

    // todo javadoc
    void deleteAllByWorkspaceId(String workspaceId);

    /**
     * Finds all {@link ProcessRole} entities by a collection of IDs, where the IDs may
     * represent either object IDs or composite IDs (containing a network ID and an object ID).
     *
     * @param ids a collection of process role IDs to filter
     * @return a {@link Set} of {@link ProcessRole} entities
     */
    default Set<ProcessRole> findAllByIdsSet(Collection<String> ids) {
        Pair<List<ObjectId>, List<ProcessResourceId>> resultPair = resolveIdsOfStrings(ids);
        Set<ProcessRole> processRoles = new HashSet<>();
        processRoles.addAll(findByObjectIds(resultPair.getFirst()));
        processRoles.addAll(findByProcessResourceIds(resultPair.getSecond()));
        return new HashSet<>(processRoles);
    }

    /**
     * Finds all {@link ProcessRole} entities by workspaceId and a collection of IDs, where the IDs may
     * represent either object IDs or composite IDs (containing a network ID and an object ID).
     *
     * @param ids a collection of process role IDs to filter
     * @return a {@link Set} of {@link ProcessRole} entities
     */
    default Set<ProcessRole> findAllByIdsSetAndWorkspaceId(Collection<String> ids, String workspaceId) {
        Pair<List<ObjectId>, List<ProcessResourceId>> resultPair = resolveIdsOfStrings(ids);
        Set<ProcessRole> processRoles = new HashSet<>();
        processRoles.addAll(findByObjectIdsAndWorkspaceId(resultPair.getFirst(), workspaceId));
        processRoles.addAll(findByProcessResourceIdsAndWorkspaceId(resultPair.getSecond(), workspaceId));
        return processRoles;
    }

    // todo javadoc
    default Pair<List<ObjectId>, List<ProcessResourceId>> resolveIdsOfStrings(Collection<String> ids) {
        List<ObjectId> forObjectIds = new ArrayList<>();
        List<ProcessResourceId> processResourceIds = new ArrayList<>();
        ids.forEach(id -> {
            if (id.contains(ProcessResourceId.ID_SEPARATOR)) {
                processResourceIds.add(new ProcessResourceId(id));
            } else {
                forObjectIds.add(new ObjectId(id));
            }
        });

        return Pair.of(forObjectIds, processResourceIds);
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
     * Finds a {@link ProcessRole} by its composite ID and workspaceID. The composite ID may represent either a single
     * object ID or a combination of network ID and object ID.
     *
     * @param compositeId the composite ID to search for
     * @param workspaceId the ID of the workspace
     * @return an {@link Optional} containing the found {@link ProcessRole}, if any
     */
    default Optional<ProcessRole> findByCompositeIdAndWorkspaceId(String compositeId, String workspaceId) {
        String[] parts = compositeId.split(ProcessResourceId.ID_SEPARATOR);
        if (parts.length == 2) {
            String networkId = parts[0];
            ObjectId objectId = new ObjectId(parts[1]);
            return findByNetworkIdAndObjectIdAndWorkspaceId(networkId, objectId, workspaceId);
        } else {
            return findByIdObjectIdAndWorkspaceId(new ObjectId(compositeId), workspaceId);
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
     * Finds a {@link ProcessRole} by a network ID, object ID and workspace ID.
     *
     * @param networkId the short process ID
     * @param objectId  the object ID
     * @param workspaceId the ID of the workspace
     * @return an {@link Optional} containing the found {@link ProcessRole}, if any
     */
    @Query("{ '_id.shortProcessId': ?0, '_id.objectId': ?1, 'workspaceId': ?2 }")
    Optional<ProcessRole> findByNetworkIdAndObjectIdAndWorkspaceId(String networkId, ObjectId objectId, String workspaceId);

    /**
     * Finds all {@link ProcessRole} entities by a collection of composite resource IDs.
     *
     * @param compositeIds a collection of {@link ProcessResourceId} instances representing the composite IDs to filter
     * @return a {@link List} of {@link ProcessRole} entities matching the provided composite IDs
     */
    @Query("{ '_id': { $in: ?0 } }")
    List<ProcessRole> findByProcessResourceIds(Collection<ProcessResourceId> compositeIds);

    /**
     * Finds all {@link ProcessRole} entities by a collection of composite resource IDs and workspace id.
     *
     * @param compositeIds a collection of {@link ProcessResourceId} instances representing the composite IDs to filter
     * @param workspaceId the ID of the workspace
     * @return a {@link List} of {@link ProcessRole} entities matching the provided composite IDs
     */
    @Query("{ '_id': { $in: ?0 }, 'workspaceId': ?1 }")
    List<ProcessRole> findByProcessResourceIdsAndWorkspaceId(Collection<ProcessResourceId> compositeIds, String workspaceId);
}