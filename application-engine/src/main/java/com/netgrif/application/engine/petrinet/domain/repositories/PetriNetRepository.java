package com.netgrif.application.engine.petrinet.domain.repositories;

import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for handling operations on {@link PetriNet} entities.
 * Extends {@link MongoRepository} for standard CRUD operations and {@link QuerydslPredicateExecutor}
 * to support dynamic queries.
 */
public interface PetriNetRepository extends MongoRepository<PetriNet, String>, QuerydslPredicateExecutor<PetriNet> {

    // todo javadoc
    Optional<PetriNet> findBy_idAndWorkspaceId(ObjectId objectId, String workspaceId);

    // todo javadoc
    List<PetriNet> findAllBy_idInAndWorkspaceId(Collection<ObjectId> objectIds, String workspaceId);

    // todo javadoc
    Page<PetriNet> findAllByWorkspaceId(String workspaceId, Pageable pageable);

    /**
     * Finds a {@link PetriNet} entity by its import identifier.
     *
     * @param id the import identifier of the desired PetriNet.
     * @return the {@link PetriNet} entity matching the given import identifier, or {@code null} if none found.
     */
    PetriNet findByImportId(String id);

    /**
     * Finds a {@link PetriNet} entity by its import identifier.
     *
     * @param id the import identifier of the desired PetriNet.
     * @param workspaceId the ID of the workspace
     * @return the {@link PetriNet} entity matching the given import identifier, or {@code null} if none found.
     */
    PetriNet findByImportIdAndWorkspaceId(String id, String workspaceId);

    /**
     * Finds a {@link PetriNet} entity by its identifier and version.
     *
     * @param identifier the unique identifier of the PetriNet.
     * @param version    the version of the PetriNet.
     * @return the {@link PetriNet} entity matching the given identifier and version, or {@code null} if none found.
     */
    PetriNet findByIdentifierAndVersion(String identifier, Version version);

    /**
     * Finds a {@link PetriNet} entity by its identifier, version and workspaceId.
     *
     * @param identifier the unique identifier of the PetriNet.
     * @param version    the version of the PetriNet.
     * @param workspaceId the ID of the workspace
     * @return the {@link PetriNet} entity matching the given identifier and version, or {@code null} if none found.
     */
    PetriNet findByIdentifierAndVersionAndWorkspaceId(String identifier, Version version, String workspaceId);

    /**
     * Finds a page of {@link PetriNet} entities by its identifier and defaultVersion attribute
     *
     * @param identifier the unique identifier of the PetriNet.
     * @param defaultVersion if true, the default version will be found, otherwise the non-default version will be found
     * @param pageable the pagination details.
     * @return the page of {@link PetriNet} entities matching the given identifier and defaultVersion attribute
     */
    Page<PetriNet> findByIdentifierAndDefaultVersion(String identifier, boolean defaultVersion, Pageable pageable);

    /**
     * Finds a page of {@link PetriNet} entities by its identifier, defaultVersion attribute and workspaceId
     *
     * @param identifier the unique identifier of the PetriNet.
     * @param defaultVersion if true, the default version will be found, otherwise the non-default version will be found
     * @param workspaceId the ID of the workspace
     * @param pageable the pagination details.
     * @return the page of {@link PetriNet} entities matching the given identifier and defaultVersion attribute
     */
    Page<PetriNet> findByIdentifierAndDefaultVersionAndWorkspaceId(String identifier, boolean defaultVersion,
                                                                   String workspaceId, Pageable pageable);

    /**
     * Finds a paginated list of {@link PetriNet} entities by their identifier.
     *
     * @param identifier the unique identifier of the PetriNet.
     * @param pageable   the pagination details.
     * @return a {@link Page} containing the list of matching {@link PetriNet} entities.
     */
    Page<PetriNet> findByIdentifier(String identifier, Pageable pageable);

    /**
     * Finds a paginated list of {@link PetriNet} entities by their identifier and workspace id.
     *
     * @param identifier the unique identifier of the PetriNet.
     * @param workspaceId the ID of the workspace
     * @param pageable   the pagination details.
     * @return a {@link Page} containing the list of matching {@link PetriNet} entities.
     */
    Page<PetriNet> findByIdentifierAndWorkspaceId(String identifier, String workspaceId, Pageable pageable);

    /**
     * Finds a paginated list of {@link PetriNet} entities by their version.
     *
     * @param version  the version of the PetriNet.
     * @param pageable the pagination details.
     * @return a {@link Page} containing the list of matching {@link PetriNet} entities.
     */
    Page<PetriNet> findAllByVersion(Version version, Pageable pageable);

    /**
     * Finds a paginated list of {@link PetriNet} entities by their version and workspaceId.
     *
     * @param version  the version of the PetriNet.
     * @param workspaceId the ID of the workspace
     * @param pageable the pagination details.
     * @return a {@link Page} containing the list of matching {@link PetriNet} entities.
     */
    Page<PetriNet> findAllByVersionAndWorkspaceId(Version version, String workspaceId, Pageable pageable);

    /**
     * Deletes a {@link PetriNet} entity by its unique object ID.
     *
     * @param id the unique ID of the PetriNet to delete.
     */
    void deleteBy_id(ObjectId id);

    /**
     * Deletes a {@link PetriNet} entity by its unique object ID and workspaceId.
     *
     * @param id the unique ID of the PetriNet to delete.
     * @param workspaceId the ID of the workspace
     */
    void deleteBy_idAndWorkspaceId(ObjectId id, String workspaceId);

    /**
     * Finds a paginated list of {@link PetriNet} entities associated with a specific role ID.
     *
     * @param roleId   the ID of the role to filter PetriNets by
     * @param pageable the pagination details
     * @return a {@link Page} of {@link PetriNet} entities matching the specified role ID
     */
    @Query("{ 'roles.?0' : { $exists: true } }")
    Page<PetriNet> findAllByRoleId(String roleId, Pageable pageable);


    /**
     * Finds a paginated list of {@link PetriNet} entities associated with a specific role ID and workspace ID.
     *
     * @param roleId   the ID of the role to filter PetriNets by
     * @param workspaceId the ID of the workspace
     * @param pageable the pagination details
     * @return a {@link Page} of {@link PetriNet} entities matching the specified role ID
     */
    @Query("{ 'roles.?0' : { $exists: true }, 'workspaceId': ?1 }")
    Page<PetriNet> findAllByRoleIdAndWorkspaceId(String roleId, String workspaceId, Pageable pageable);

    /**
     * Find all active Petri Nets
     *
     * @param pageable the pagination details
     *
     * @return a {@link Page} of active {@link PetriNet} entities
     * */
    Page<PetriNet> findAllByDefaultVersionTrue(Pageable pageable);

    /**
     * Find all active Petri Nets within a workspace
     *
     * @param workspaceId the ID of the workspace
     * @param pageable the pagination details
     *
     * @return a {@link Page} of active {@link PetriNet} entities
     * */
    Page<PetriNet> findAllByDefaultVersionTrueAndWorkspaceId(String workspaceId, Pageable pageable);
}