package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.CaseRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRoleRepository extends MongoRepository<CaseRole, String>, QuerydslPredicateExecutor<CaseRole> {
}
