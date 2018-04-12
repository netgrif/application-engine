package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.UserState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByEmailAndState(String email, UserState state);

    User findByToken(String token);

    List<User> findAllByExpirationDateBefore(LocalDateTime dateTime);

    List<User> findAllByState(UserState state);

    List<User> findByUserProcessRoles_RoleIdIn(List<String> roleId);
}