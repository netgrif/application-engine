package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.UserProcessRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

public interface UserProcessRoleRepository extends JpaRepository<UserProcessRole, Long>, QueryDslPredicateExecutor<UserProcessRole> {

    List<UserProcessRole> findByRoleIdIn(Iterable<String> roleIds);

    UserProcessRole findByRoleId(String roleId);

    List<UserProcessRole> findAllByNetId(String id);
}