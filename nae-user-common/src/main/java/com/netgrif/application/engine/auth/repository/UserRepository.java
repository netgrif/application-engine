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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    default Page<User> findAll(Predicate predicate, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        Assert.notNull(predicate, "Predicate must not be null");
        Assert.notNull(pageable, "Pageable must not be null");

        SpringDataMongodbQuery<com.netgrif.application.engine.adapter.spring.auth.domain.User> query = createQuery(predicate, mongoTemplate, collection);
        return query.fetchPage(pageable).map(User.class::cast);
    }

    default Optional<User> findById(ObjectId objectId, MongoTemplate mongoTemplate, String collectionName) {
        return Optional.ofNullable(
                mongoTemplate.findOne(Query.query(Criteria.where("id").is(objectId)), com.netgrif.application.engine.adapter.spring.auth.domain.User.class, collectionName)
        );
    }

    default Optional<User> findByUsername(String username, MongoTemplate mongoTemplate, String collectionName) {
        return Optional.ofNullable(
                mongoTemplate.findOne(Query.query(Criteria.where("username").is(username)), com.netgrif.application.engine.adapter.spring.auth.domain.User.class, collectionName)
        );
    }

    default Optional<User> findByEmail(String email, MongoTemplate mongoTemplate, String collectionName) {
        return Optional.ofNullable(
                mongoTemplate.findOne(Query.query(Criteria.where("email").is(email)), com.netgrif.application.engine.adapter.spring.auth.domain.User.class, collectionName)
        );
    }

    default User saveUser(User user, MongoTemplate mongoTemplate, String collectionName) {
        return mongoTemplate.save(user, collectionName);
    }

    default void deleteAll(MongoTemplate mongoTemplate, Collection<String> collectionName) {
        collectionName.forEach(collection -> mongoTemplate.remove(new Query(), collection));
    }

    default Page<User> findDistinctByStateAndProcessRoles__idIn(UserState state, List<ProcessResourceId> roleId, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        Query query = Query.query(
                Criteria.where("state").is(state)
                        .and("processRoles._id").in(roleId));
        return resolveUserPage(pageable, mongoTemplate, collection, query);
    }

    default Page<User> findAllByProcessRoles__idIn(List<ProcessResourceId> rolesId, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        Query query = Query.query(
                Criteria.where("processRoles._id").in(rolesId));
        return resolveUserPage(pageable, mongoTemplate, collection, query);
    }

    default void removeAllByStateAndExpirationDateBefore(UserState state, LocalDateTime dateTime, MongoTemplate mongoTemplate, Set<String> collectionNames) {
        collectionNames.forEach(collectionName ->
                mongoTemplate.remove(Query.query(Criteria.where("state").is(state).and("credentials.token.properties.expirationDate").lt(dateTime)), com.netgrif.application.engine.adapter.spring.auth.domain.User.class, collectionName)
        );
    }

    default Page<User> findAllByStateAndExpirationDateBefore(UserState state, LocalDateTime dateTime, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        Query query = Query.query(
                Criteria.where("state").is(state)
                        .and("credentials.token.properties.expirationDate").lt(dateTime));
        return resolveUserPage(pageable, mongoTemplate, collection, query);
    }

    default Page<User> findAllByIdInAndState(Set<ObjectId> ids, UserState state, Pageable pageable, MongoTemplate mongoTemplate, String collection) {
        Query query = Query.query(
                        Criteria.where("id").in(ids)
                                .and("state").is(state));
        return resolveUserPage(pageable, mongoTemplate, collection, query);
    }

    private SpringDataMongodbQuery<com.netgrif.application.engine.adapter.spring.auth.domain.User> createQuery(Predicate predicate, MongoTemplate mongoTemplate, String collectionName) {
        return new SpringDataMongodbQuery<>(mongoTemplate, com.netgrif.application.engine.adapter.spring.auth.domain.User.class, collectionName).where(predicate);
    }

    private static PageImpl<User> resolveUserPage(Pageable pageable, MongoTemplate mongoTemplate, String collection, Query query) {
        List<User> resultUserList = mongoTemplate.find(
                        query.with(pageable),
                        com.netgrif.application.engine.adapter.spring.auth.domain.User.class,
                        collection)
                .stream()
                .map(User.class::cast)
                .collect(Collectors.toList());
        long total = mongoTemplate.count(query.limit(-1).skip(-1), com.netgrif.application.engine.adapter.spring.auth.domain.User.class);
        return new PageImpl<>(resultUserList, pageable, total);
    }
}
