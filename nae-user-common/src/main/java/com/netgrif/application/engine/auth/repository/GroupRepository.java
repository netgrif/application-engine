package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.objects.auth.domain.User;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Group} entities in the MongoDB database.
 * Extends the {@link MongoRepository} interface for basic CRUD operations.
 */
@Repository
public interface GroupRepository extends MongoRepository<Group, String>, QuerydslPredicateExecutor<Group> {


    /**
     * Checks if a {@link Group} entity exists with the given identifier.
     *
     * @param identifier the unique identifier of the group
     * @return {@code true} if a group with the specified identifier exists, otherwise {@code false}
     */
    boolean existsByIdentifier(String identifier);

    /**
     * Finds paginated list of all {@link Group} entities that have the given owner ID.
     *
     * @param id the ID of the owner
     * @param pageable the pagination information
     * @return a set of {@link Group} entities associated with the specified owner ID
     */
    Page<Group> findByOwnerId(String id, Pageable pageable);

    /**
     * Finds a {@link Group} by its unique identifier.
     *
     * @param identifier the unique identifier of the group
     * @return an {@link Optional} containing the {@link Group} if it exists, otherwise {@code Optional.empty()}
     */
    Optional<Group> findByIdentifier(String identifier);

    /**
     * Finds all {@link Group}s with given IDs in a pageable format.
     *
     * @param ids the collection of group IDs to query
     * @param pageable the pagination information
     * @return a {@link Page} of {@link Group}s with the specified IDs
     */
    Page<Group> findAllByIdIn(Collection<String> ids, Pageable pageable);

    /**
     * Finds all {@link Group}s associated with a particular realm ID in a pageable format.
     *
     * @param realmId the ID of the realm
     * @param pageable the pagination information
     * @return a {@link Page} of {@link Group}s belonging to the specified realm
     */
    Page<Group> findAllByRealmId(String realmId, Pageable pageable);

    Page<Group> findAllByRealmIdIn(Collection<String> realmIds, Pageable pageable);

    Page<Group> findAllByProcessRoles__idIn(Collection<ProcessResourceId> rolesId, Pageable pageable);

    void removeAllByRealmIdIn(Collection<String> realmIds);

    void removeAllByRealmId(String realmId);

    default Page<Group> findAll(Query query, MongoTemplate mongoTemplate, Pageable pageable) {
        if (query == null) {
            query = new Query();
        }
        List<Group> resultGroupList = mongoTemplate.find(
                query.with(pageable),
                Group.class,
                "group"
        );
        long total = mongoTemplate.count(query.limit(-1).skip(-1), Group.class, "group");
        return new PageImpl<>(resultGroupList, pageable, total);
    }
}
