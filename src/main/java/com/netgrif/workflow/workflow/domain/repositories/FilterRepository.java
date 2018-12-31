package com.netgrif.workflow.workflow.domain.repositories;


import com.netgrif.workflow.workflow.domain.Filter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRepository extends MongoRepository<Filter, String> {
}