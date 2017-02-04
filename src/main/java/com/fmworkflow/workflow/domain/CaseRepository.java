package com.fmworkflow.workflow.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CaseRepository extends MongoRepository<Case, String>{
}
