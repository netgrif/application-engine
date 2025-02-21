package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.CaseRoleAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRoleAssignmentRepository extends MongoRepository<CaseRoleAssignment, String>, QuerydslPredicateExecutor<CaseRoleAssignment> {
}
