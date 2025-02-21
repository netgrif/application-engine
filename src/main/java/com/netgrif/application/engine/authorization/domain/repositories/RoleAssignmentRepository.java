package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleAssignmentRepository extends MongoRepository<RoleAssignment, String>, QuerydslPredicateExecutor<RoleAssignment> {
}
