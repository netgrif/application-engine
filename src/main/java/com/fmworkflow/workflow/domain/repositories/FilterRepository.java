package com.fmworkflow.workflow.domain.repositories;


import com.fmworkflow.workflow.domain.Filter;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FilterRepository extends MongoRepository<Filter, String> {
}
