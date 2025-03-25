package com.netgrif.application.engine.authentication.domain.repositories;

import com.netgrif.application.engine.authentication.domain.IdentityState;
import com.netgrif.application.engine.authentication.domain.QUser;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends MongoRepository<User, String>, QuerydslPredicateExecutor<User>, QuerydslBinderCustomizer<QUser> {

    Page<User> findAllByIdInAndState(Set<ObjectId> ids, IdentityState state, Pageable pageable);

    User findByEmail(String email);

    List<User> findAllByStateAndExpirationDateBefore(IdentityState identityState, LocalDateTime dateTime);

    List<User> removeAllByStateAndExpirationDateBefore(IdentityState state, LocalDateTime dateTime);

    List<User> findAllByIdIn(Set<ObjectId> ids);

    Page<User> findAllByIdIn(Collection<ObjectId> ids, Pageable pageable);

    boolean existsByEmail(String email);

    @Override
    default void customize(QuerydslBindings bindings, QUser qUser) {
    }
}