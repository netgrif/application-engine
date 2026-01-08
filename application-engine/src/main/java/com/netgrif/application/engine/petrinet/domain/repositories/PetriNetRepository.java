package com.netgrif.application.engine.petrinet.domain.repositories;

import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * Repository interface for handling operations on {@link PetriNet} entities.
 * Extends {@link MongoRepository} for standard CRUD operations and {@link QuerydslPredicateExecutor}
 * to support dynamic queries.
 */
public interface PetriNetRepository extends MongoRepository<PetriNet, String>, QuerydslPredicateExecutor<PetriNet> {

    /**
     * Finds a {@link PetriNet} entity by its import identifier.
     *
     * @param id the import identifier of the desired PetriNet.
     * @return the {@link PetriNet} entity matching the given import identifier, or {@code null} if none found.
     */
    PetriNet findByImportId(String id);

    /**
     * Finds a {@link PetriNet} entity by its identifier and version.
     *
     * @param identifier the unique identifier of the PetriNet.
     * @param version    the version of the PetriNet.
     * @return the {@link PetriNet} entity matching the given identifier and version, or {@code null} if none found.
     */
    PetriNet findByIdentifierAndVersion(String identifier, Version version);

    /**
     * Finds a {@link PetriNet} entity by its identifier and defaultVersion attribute
     *
     * @param identifier the unique identifier of the PetriNet.
     * @param defaultVersion if true, the default version will be found, otherwise the non-default version will be found
     * @param pageable the pagination details.
     * @return the {@link PetriNet} entity matching the given identifier and defaultVersion attribute, or {@code null} if none found.
     */
    Page<PetriNet> findByIdentifierAndDefaultVersion(String identifier, boolean defaultVersion, Pageable pageable);

    /**
     * Finds a paginated list of {@link PetriNet} entities by their identifier.
     *
     * @param identifier the unique identifier of the PetriNet.
     * @param pageable   the pagination details.
     * @return a {@link Page} containing the list of matching {@link PetriNet} entities.
     */
    Page<PetriNet> findByIdentifier(String identifier, Pageable pageable);

    /**
     * Finds a paginated list of {@link PetriNet} entities by their version.
     *
     * @param version  the version of the PetriNet.
     * @param pageable the pagination details.
     * @return a {@link Page} containing the list of matching {@link PetriNet} entities.
     */
    Page<PetriNet> findAllByVersion(Version version, Pageable pageable);

    /**
     * Deletes a {@link PetriNet} entity by its unique object ID.
     *
     * @param id the unique ID of the PetriNet to delete.
     */
    void deleteBy_id(ObjectId id);


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
     * Find all active Petri Nets
     *
     * @param pageable the pagination details
     *
     * @return a {@link Page} of active {@link PetriNet} entities
     * */
    Page<PetriNet> findAllByVersionActiveTrue(Pageable pageable);
}