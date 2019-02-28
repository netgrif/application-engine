package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, QueryDslPredicateExecutor<User> {

    Page<User> findAllByIdInAndState(Set<Long> ids, UserState state, Pageable pageable);

    User findByEmail(String email);

    List<User> findAllByStateAndExpirationDateBefore(UserState userState, LocalDateTime dateTime);

    Page<User> findDistinctByStateAndUserProcessRoles_RoleIdIn(UserState state, List<String> roleId, Pageable pageable);

    List<User> removeAllByStateAndExpirationDateBefore(UserState state, LocalDateTime dateTime);

    boolean existsByEmail(String email);
}