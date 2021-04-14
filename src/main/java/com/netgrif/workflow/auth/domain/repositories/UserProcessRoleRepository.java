package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.UserProcessRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProcessRoleRepository extends MongoRepository<UserProcessRole, String>, QuerydslPredicateExecutor<UserProcessRole> {

    List<UserProcessRole> findByRoleIdIn(Iterable<String> roleIds);

    UserProcessRole findByRoleId(String roleId);

    List<UserProcessRole> findAllByNetId(String id);

    void deleteAllByNetId(String id);
}