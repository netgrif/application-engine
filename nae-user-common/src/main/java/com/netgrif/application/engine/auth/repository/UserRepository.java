package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.adapter.spring.utils.PageableUtils;
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

@Repository
public interface UserRepository extends MongoRepository<User, String>, QuerydslPredicateExecutor<User> {

    @Override
    default <S extends User> S save(S entity) {
        throw new UnsupportedOperationException("This method is not supported. Use 'UserRepository.saveUser' instead.'");
    }

    @Override
    default Page<User> findAll(Predicate predicate, Pageable pageable) {
        throw new UnsupportedOperationException("This method is not supported. Use 'UserRepository.findAll(Predicate, Pageable, MongoTemplate, Collection<String>)' instead.'");
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException("This method is not supported. Use 'UserRepository.deleteAll(MongoTemplate, Collection<String>)' instead.'");
    }

    default Page<User> findAll(Predicate predicate, Pageable pageable, MongoTemplate mongoTemplate, Collection<String> collectionNames) {
        Assert.notNull(predicate, "Predicate must not be null");
        Assert.notNull(pageable, "Pageable must not be null");

        return PageableUtils.listToPage(collectionNames.stream().map(collection -> {
            SpringDataMongodbQuery<User> query = createQuery(predicate, mongoTemplate, collection);
            return query.fetch().stream().map(User.class::cast).toList();
        }).flatMap(List::stream).toList(), pageable);
    }

    default Optional<User> findById(ObjectId objectId, MongoTemplate mongoTemplate, String collectionName) {
        return Optional.ofNullable(
                mongoTemplate.findOne(Query.query(Criteria.where("id").is(objectId)), User.class, collectionName)
        );
    }

    default List<User> findAllByIds(Collection<ObjectId> objectIds, MongoTemplate mongoTemplate, String collectionName) {
        return mongoTemplate.find(Query.query(Criteria.where("id").in(objectIds)), User.class, collectionName);
    }

    default Optional<User> findByUsername(String username, MongoTemplate mongoTemplate, String collectionName) {
        return Optional.ofNullable(
                mongoTemplate.findOne(Query.query(Criteria.where("username").is(username)), User.class, collectionName)
        );
    }

    default Optional<User> findByEmail(String email, MongoTemplate mongoTemplate, String collectionName) {
        return Optional.ofNullable(
                mongoTemplate.findOne(Query.query(Criteria.where("email").is(email)), User.class, collectionName)
        );
    }

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

    default Page<User> findDistinctByStateAndProcessRoles__idIn(UserState state, Collection<ProcessResourceId> roleId, Pageable pageable, MongoTemplate mongoTemplate, Collection<String> collectionNames) {
        Set<User> resultUserSet = collectionNames.stream().map(collectionName ->
                mongoTemplate.find(
                        Query.query(
                                        Criteria.where("state").is(state)
                                                .and("processRoles._id").in(roleId))
                                .with(pageable),
                        User.class,
                        collectionName)
        ).flatMap(List::stream).collect(Collectors.toSet());
        return new PageImpl<>(resultUserSet.stream().toList(), pageable, resultUserSet.size());
    }

    default List<User> findAllByProcessRoles__idIn(Collection<ProcessResourceId> rolesId, MongoTemplate mongoTemplate, Collection<String> collectionNames) {
        return collectionNames.stream().map(collectionName ->
                mongoTemplate.find(Query.query(Criteria.where("processRoles._id").in(rolesId)), User.class, collectionName)
        ).flatMap(List::stream).map(User.class::cast).collect(Collectors.toList());
    }

    default void removeAllByStateAndExpirationDateBefore(UserState state, LocalDateTime dateTime, MongoTemplate mongoTemplate, Collection<String> collectionNames) {
        collectionNames.forEach(collectionName ->
                mongoTemplate.remove(Query.query(Criteria.where("state").is(state).and("credentials.token.properties.expirationDate").lt(dateTime)), User.class, collectionName)
        );
    }

    default List<User> findAllByStateAndExpirationDateBefore(UserState state, LocalDateTime dateTime, MongoTemplate mongoTemplate, Collection<String> collectionNames) {
        return collectionNames.stream().map(collectionName ->
                mongoTemplate.find(Query.query(Criteria.where("state").is(state).and("credentials.token.properties.expirationDate").lt(dateTime)), User.class, collectionName)
        ).flatMap(List::stream).map(User.class::cast).collect(Collectors.toList());
    }

    default Page<User> findAllByIdInAndState(Collection<ObjectId> ids, UserState state, Pageable pageable, MongoTemplate mongoTemplate, Collection<String> collectionNames) {
        Set<User> resultUserSet = collectionNames.stream().map(collectionName ->
                mongoTemplate.find(
                        Query.query(
                                        Criteria.where("id").in(ids)
                                                .and("state").is(state))
                                .with(pageable),
                        User.class,
                        collectionName)
        ).flatMap(List::stream).collect(Collectors.toSet());
        return new PageImpl<>(resultUserSet.stream().toList(), pageable, resultUserSet.size());
    }

    private SpringDataMongodbQuery<User> createQuery(Predicate predicate, MongoTemplate mongoTemplate, String collectionName) {
        return new SpringDataMongodbQuery<>(mongoTemplate, User.class, collectionName).where(predicate);
    }
}
