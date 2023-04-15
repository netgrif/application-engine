package com.netgrif.application.engine.auth.domain.repositories;

import com.netgrif.application.engine.auth.domain.QUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.auth.domain.UserState;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends MongoRepository<User, String>, QuerydslPredicateExecutor<User>, QuerydslBinderCustomizer<QUser> {

    Page<User> findAllByIdInAndState(Set<ObjectId> ids, UserState state, Pageable pageable);

    User findByEmail(String email);

    List<User> findAllByStateAndExpirationDateBefore(UserState userState, LocalDateTime dateTime);

    Page<User> findDistinctByStateAndProcessRoles_IdIn(UserState state, List<String> roleId, Pageable pageable);

    List<User> findAllByProcessRoles_IdIn(List<String> roleId);

    List<User> removeAllByStateAndExpirationDateBefore(UserState state, LocalDateTime dateTime);

    List<User> findAllByIdIn(Set<ObjectId> ids);

    boolean existsByEmail(String email);

    @Override
    default void customize(QuerydslBindings bindings, QUser qUser) {
    }
}