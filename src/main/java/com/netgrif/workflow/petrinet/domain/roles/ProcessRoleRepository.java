package com.netgrif.workflow.petrinet.domain.roles;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProcessRoleRepository extends MongoRepository<ProcessRole, String> {

    Set<ProcessRole> findAllById(Iterable<String> strings);

    Set<ProcessRole> findAllBy_idIn(Set<String> ids);

    Set<ProcessRole> findAllByImportIdIn(Set<String> importIds);

    ProcessRole findByName_DefaultValue(String name);
}