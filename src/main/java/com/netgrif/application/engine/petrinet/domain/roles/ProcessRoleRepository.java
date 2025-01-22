package com.netgrif.application.engine.petrinet.domain.roles;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface ProcessRoleRepository extends MongoRepository<ProcessRole, String>, QuerydslPredicateExecutor<ProcessRole> {

    Set<ProcessRole> findAllByImportIdIn(Set<String> importIds);

    Set<ProcessRole> findAllByName_DefaultValue(String name);

    Set<ProcessRole> findAllByImportId(String importId);

    void deleteAllByIdIn(Collection<ObjectId> ids);
}