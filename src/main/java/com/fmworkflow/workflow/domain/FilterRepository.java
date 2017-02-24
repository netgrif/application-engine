package com.fmworkflow.workflow.domain;


import org.springframework.data.mongodb.repository.MongoRepository;

public interface FilterRepository extends MongoRepository<Filter, String>{
}
