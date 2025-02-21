package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.ProcessRole;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface ProcessRoleRepository extends MongoRepository<ProcessRole, String>, QuerydslPredicateExecutor<ProcessRole> {

    Set<ProcessRole> findAllByImportIdIn(Set<String> importIds);

    Set<ProcessRole> findAllByTitle_DefaultValue(String name);

    Set<ProcessRole> findAllByImportId(String importId);

    void deleteAllByIdIn(Collection<ObjectId> ids);

    boolean existsByImportId(String importId);
}