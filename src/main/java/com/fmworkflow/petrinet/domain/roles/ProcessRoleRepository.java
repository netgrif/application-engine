package com.fmworkflow.petrinet.domain.roles;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessRoleRepository extends MongoRepository<ProcessRole, String> {
}