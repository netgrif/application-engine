package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.objects.auth.domain.User;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.querydsl.core.types.Predicate;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.support.SpringDataMongodbQuery;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository interface for managing {@link User} entities in the MongoDB database.
 * Extends the {@link MongoRepository} interface for basic CRUD operations.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String>, QuerydslPredicateExecutor<User> {

    /**
     * Throws an exception since this method is not supported.
     * Use {@link #saveUser(User, MongoTemplate, String)} instead.
     *
     * @param entity the entity to save
     * @param <S>    subtype of {@link User}
     * @return never returns, always throws exception
     * @throws UnsupportedOperationException as this method is not supported
     */
    @Override
    default <S extends User> S save(S entity) {
        throw new UnsupportedOperationException("This method is not supported. Use 'UserRepository.saveUser' instead.'");
    }

    /**
     * Throws an exception since this method is not supported.
     * Use {@link #findAllByQuery(Predicate, Pageable, MongoTemplate, String)} instead.
     *
     * @param predicate filter condition
     * @param pageable  pagination details
     * @return never returns, always throws exception
     * @throws UnsupportedOperationException as this method is not supported
     */
    @Override
    default Page<User> findAll(Predicate predicate, Pageable pageable) {
        throw new UnsupportedOperationException("This method is not supported. Use 'UserRepository.findAll(Predicate, Pageable, MongoTemplate, Collection<String>)' instead.'");
    }

    /**
     * Throws an exception since this method is not supported.
     * Use {@link #deleteAll(MongoTemplate, Collection)} instead.
     *
     * @throws UnsupportedOperationException as this method is not supported
     */
    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException("This method is not supported. Use 'UserRepository.deleteAll(MongoTemplate, Collection<String>)' instead.'");
    }

    /**
     * Finds a paginated list of all {@link User} entities matching the given {@link Predicate}.
     *
     * @param predicate     the condition to filter users
     * @param pageable      pagination details
     * @param mongoTemplate the MongoDB template
     * @param collection    the target collection
     * @return paginated result of users matching the condition
     */
    default Page<User> findAllByQuery(Predicate predicate, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        Assert.notNull(predicate, "Predicate must not be null");
        Assert.notNull(pageable, "Pageable must not be null");

        SpringDataMongodbQuery<User> query = createQuery(predicate, mongoTemplate, collection);
        return query.fetchPage(pageable);
    }

    /**
     * Finds a {@link User} by its ObjectId.
     *
     * @param objectId       the ID of the user to search
     * @param mongoTemplate  the MongoDB template
     * @param collectionName the collection name
     * @return optional of the user if found, otherwise empty
     */
    default Optional<User> findById(ObjectId objectId, MongoTemplate mongoTemplate, String collectionName) {
        return Optional.ofNullable(
                mongoTemplate.findOne(Query.query(Criteria.where("id").is(objectId)), User.class, collectionName)
        );
    }

    default List<User> findAllByIds(Collection<ObjectId> objectIds, MongoTemplate mongoTemplate, String collectionName) {
        return mongoTemplate.find(Query.query(Criteria.where("id").in(objectIds)), User.class, collectionName);
    }

    /**
     * Finds a {@link User} by their username.
     *
     * @param username       the username to search for
     * @param mongoTemplate  the MongoDB template
     * @param collectionName the collection name
     * @return optional of the user if found, otherwise empty
     */
    default Optional<User> findByUsername(String username, MongoTemplate mongoTemplate, String collectionName) {
        return Optional.ofNullable(
                mongoTemplate.findOne(Query.query(Criteria.where("username").is(username)), User.class, collectionName)
        );
    }

    /**
     * Finds a {@link User} by their email address.
     *
     * @param email          the email to search for
     * @param mongoTemplate  the MongoDB template
     * @param collectionName the collection name
     * @return optional of the user if found, otherwise empty
     */
    default Optional<User> findByEmail(String email, MongoTemplate mongoTemplate, String collectionName) {
        return Optional.ofNullable(
                mongoTemplate.findOne(Query.query(Criteria.where("email").is(email)), User.class, collectionName)
        );
    }

    /**
     * Saves a {@link User} into the specified collection.
     *
     * @param user           the user to save
     * @param mongoTemplate  the MongoDB template
     * @param collectionName the collection to save the user in
     * @return the saved user
     */
    default User saveUser(User user, MongoTemplate mongoTemplate, String collectionName) {
        return mongoTemplate.save(user, collectionName);
    }

    default void deleteAllByIdFromCollection(MongoTemplate mongoTemplate, Collection<ObjectId> userIds, String collection) {
        mongoTemplate.remove(
                new Query(Criteria.where("id").in(userIds)),
                User.class,
                collection
        );
    }

    default void deleteAllFromCollection(MongoTemplate mongoTemplate, String collection) {
        mongoTemplate.remove(new Query(), collection);
    }

    default void deleteAll(MongoTemplate mongoTemplate, Collection<String> collectionName) {
        collectionName.forEach(collection -> mongoTemplate.remove(new Query(), collection));
    }

    /**
     * Finds a paginated list of all {@link User} entities using a specified {@link Query}
     *
     * @param query         the Mongo query to execute; if null, a new {@link Query} object will be created.
     * @param pageable      the pagination information to apply to the query results.
     * @param mongoTemplate the MongoDB template used to perform the query.
     * @param collection    the name of the collection where the query will be executed.
     * @return a {@link Page} of {@link User} entities obtained from the query results.
     */
    default Page<User> findAllByQuery(Query query, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        if (query == null) {
            query = new Query();
        }
        return resolveUserPage(pageable, mongoTemplate, collection, query);
    }

    /**
     * Finds a paginated list of distinct {@link User}s by their {@link UserState} and process role IDs.
     *
     * @param state         the state of the users
     * @param roleId        the IDs of the process roles
     * @param pageable      pagination details
     * @param mongoTemplate the MongoDB template
     * @param collection    the collection name
     * @return a paginated list of distinct users with the specified state and process roles
     */
    default Page<User> findDistinctByStateAndProcessRoles__idIn(UserState state, Collection<ProcessResourceId> roleId, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        Query query = Query.query(
                Criteria.where("state").is(state)
                        .and("processRoles._id").in(roleId));
        return resolveUserPage(pageable, mongoTemplate, collection, query);
    }

    /**
     * Finds a paginated list of all {@link User} entities by their associated process role IDs.
     *
     * @param rolesId       the collection of {@link ProcessResourceId}s to filter users by.
     * @param pageable      the pagination details for the query result.
     * @param mongoTemplate the MongoDB template used to perform the query.
     * @param collection    the name of the collection where the query will be executed.
     * @return a {@link Page} containing the {@link User} entities that match the specified process role IDs.
     */
    default Page<User> findAllByProcessRoles__idIn(Collection<ProcessResourceId> rolesId, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        Query query = Query.query(
                Criteria.where("processRoles._id").in(rolesId));
        return resolveUserPage(pageable, mongoTemplate, collection, query);
    }

    /**
     * Removes all {@link User} entities with the specified state and an expiration date before the given time.
     *
     * @param state           the {@link UserState} to filter users.
     * @param dateTime        the expiration date before which users will be deleted.
     * @param mongoTemplate   the MongoDB template used to perform the operation.
     * @param collectionNames the set of collection names in which the deletion will be performed.
     */
    default void removeAllByStateAndExpirationDateBefore(UserState state, LocalDateTime dateTime, MongoTemplate mongoTemplate, Set<String> collectionNames) {
        collectionNames.forEach(collectionName ->
                mongoTemplate.remove(Query.query(Criteria.where("state").is(state).and("credentials.token.properties.expirationDate").lt(dateTime)), User.class, collectionName)
        );
    }

    /**
     * Finds a paginated list of all {@link User} entities with the specified state and an expiration date before the given time
     *
     * @param state         the {@link UserState} to filter the users.
     * @param dateTime      the expiration date before which users should be filtered.
     * @param pageable      the pagination details for the query result.
     * @param mongoTemplate the MongoDB template used to perform the query.
     * @param collection    the name of the collection where the query will be executed.
     * @return a {@link Page} containing {@link User} entities matching the specified state and expiration date criteria.
     */
    default Page<User> findAllByStateAndExpirationDateBefore(UserState state, LocalDateTime dateTime, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        Query query = Query.query(
                Criteria.where("state").is(state)
                        .and("credentials.token.properties.expirationDate").lt(dateTime));
        return resolveUserPage(pageable, mongoTemplate, collection, query);
    }

    /**
     * Finds a paginated list of all {@link User}s by their IDs.
     *
     * @param ids           the list of user IDs
     * @param pageable      pagination details
     * @param mongoTemplate the MongoDB template
     * @param collection    the collection name
     * @return a paginated list of users with the specified IDs
     */
    default Page<User> findAllByIds(Collection<ObjectId> ids, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        Query query = Query.query(
                Criteria.where("id").in(ids));
        return resolveUserPage(pageable, mongoTemplate, collection, query);
    }

    /**
     * Creates a {@link SpringDataMongodbQuery} for {@link User}.
     *
     * @param predicate      the filter condition to apply to the query.
     * @param mongoTemplate  the MongoDB template used to execute the query.
     * @param collectionName the name of the collection to query.
     * @return a {@link SpringDataMongodbQuery} configured with the given predicate and collection name.
     */
    private SpringDataMongodbQuery<User> createQuery(Predicate predicate, MongoTemplate mongoTemplate, String collectionName) {
        return new SpringDataMongodbQuery<>(mongoTemplate, User.class, collectionName).where(predicate);
    }

    /**
     * Resolves a {@link PageImpl} of {@link User} entities.
     * Helper method to execute queries and handle pagination.
     *
     * @param pageable      the pagination information
     * @param mongoTemplate the MongoDB template
     * @param collection    the collection name
     * @param query         the query to execute
     * @return a paginated implementation of users
     */
    private static PageImpl<User> resolveUserPage(Pageable pageable, MongoTemplate mongoTemplate, String collection, Query query) {
        List<User> resultUserList = mongoTemplate.find(
                        query.with(pageable),
                        User.class,
                        collection);
        long total = mongoTemplate.count(query.limit(-1).skip(-1), User.class, collection);
        return new PageImpl<>(resultUserList, pageable, total);
    }

    // todo javadoc
    default Page<User> findAllByWorkspacePermission(String workspaceId, Pageable pageable, MongoTemplate mongoTemplate,
                                                    String collectionName) {
        Query query = Query.query(Criteria.where("workspacePermissions.%s".formatted(workspaceId)).exists(true));
        return resolveUserPage(pageable, mongoTemplate, collectionName, query);
    }
}
