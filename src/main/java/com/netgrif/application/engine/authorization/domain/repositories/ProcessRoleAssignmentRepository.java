package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.ProcessRoleAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessRoleAssignmentRepository extends MongoRepository<ProcessRoleAssignment, String>, QuerydslPredicateExecutor<ProcessRoleAssignment> {
}
