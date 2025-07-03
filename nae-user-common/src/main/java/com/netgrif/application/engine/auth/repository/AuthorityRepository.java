package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Authority} entities in the MongoDB database.
 * Extends the {@link MongoRepository} interface for basic CRUD operations.
 */
@Repository
public interface AuthorityRepository extends MongoRepository<Authority, String> {

    /**
     * Finds an {@link Authority} entity by its name.
     *
     * @param name the name of the authority to search for
     * @return an {@link Optional} containing the {@link Authority} if found, or empty if not found
     */
    Optional<Authority> findByName(String name);

    /**
     * Retrieves a paginated list of {@link Authority} entities whose IDs match a given list of {@link ObjectId}s.
     *
     * @param ids a list of {@link ObjectId}s to search for
     * @param pageable the pagination information defined by a {@link Pageable} object
     * @return a {@link Page} containing the matching {@link Authority} entities
     */
    Page<Authority> findAllBy_idIn(List<ObjectId> ids, Pageable pageable);
}