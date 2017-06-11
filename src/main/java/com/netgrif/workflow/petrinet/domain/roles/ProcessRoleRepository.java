package com.netgrif.workflow.petrinet.domain.roles;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Set;

public interface ProcessRoleRepository extends MongoRepository<ProcessRole, String> {
    Set<ProcessRole> findAll(Iterable<String> strings);
}