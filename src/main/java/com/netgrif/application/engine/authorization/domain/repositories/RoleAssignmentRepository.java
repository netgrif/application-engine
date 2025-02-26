package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RoleAssignmentRepository extends MongoRepository<RoleAssignment, String>, QuerydslPredicateExecutor<RoleAssignment> {
    Iterable<RoleAssignment> findAllByUserIdAndRoleIdIn(String userId, Set<String> roleIds);
    Iterable<RoleAssignment> removeAllByUserIdAndRoleIdIn(String userId, Set<String> roleIds);
    RoleAssignment removeByUserIdAndRoleId(String userId, String roleId);
    Iterable<RoleAssignment> removeAllByUserId(String userId);
    Iterable<RoleAssignment> removeAllByRoleId(String roleId);
    Iterable<RoleAssignment> removeAllByRoleIdIn(Set<String> roleIds);
    boolean existsByUserIdAndRoleId(String userId, String roleId);
}
