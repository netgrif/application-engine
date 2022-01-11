package com.netgrif.workflow.petrinet.domain.roles;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface ProcessRoleRepository extends MongoRepository<ProcessRole, String>, QuerydslPredicateExecutor<ProcessRole> {

    Set<ProcessRole> findAllById(Iterable<String> strings);

    Set<ProcessRole> findAllByNetId(String netId);

    Set<ProcessRole> findAllBy_idIn(Set<String> ids);

    ProcessRole findBy_id(String id);

    Set<ProcessRole> findAllByImportIdIn(Set<String> importIds);

    ProcessRole findByName_DefaultValue(String name);

    ProcessRole findByImportId(String importId);

    void deleteAllBy_idIn(Collection<ObjectId> ids);
}