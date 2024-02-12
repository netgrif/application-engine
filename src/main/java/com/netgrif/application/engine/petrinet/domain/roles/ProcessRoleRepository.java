package com.netgrif.application.engine.petrinet.domain.roles;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface ProcessRoleRepository extends MongoRepository<ProcessRole, String>, QuerydslPredicateExecutor<ProcessRole> {

    Set<ProcessRole> findAllByNetId(String netId);

    Set<ProcessRole> findAllByImportIdIn(Set<String> importIds);

    Set<ProcessRole> findAllByName_DefaultValue(String name);

    Set<ProcessRole> findAllByImportId(String importId);

    void deleteAllBy_idIn(Collection<ObjectId> ids);
}