package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RoleAssignmentRepository extends MongoRepository<RoleAssignment, String>, QuerydslPredicateExecutor<RoleAssignment> {
    Iterable<RoleAssignment> findAllByActorIdAndRoleIdIn(String actorId, Set<String> roleIds);
    Iterable<RoleAssignment> findAllByRoleIdIn(Set<String> roleIds);
    Iterable<RoleAssignment> findAllByActorId(String actorId);
    Iterable<RoleOnly> findAllRoleIdsByActorId(String actorId);
    boolean existsByActorIdAndRoleId(String actorId, String roleId);

    Iterable<RoleAssignment> removeAllByActorIdAndRoleIdIn(String actorId, Set<String> roleIds);
    RoleAssignment removeByActorIdAndRoleId(String actorId, String roleId);
    Iterable<RoleAssignment> removeAllByActorId(String actorId);
    Iterable<RoleAssignment> removeAllByRoleId(String roleId);
    Iterable<RoleAssignment> removeAllByRoleIdIn(Set<String> roleIds);

    interface RoleOnly {
        String getRoleId();
    }
}
