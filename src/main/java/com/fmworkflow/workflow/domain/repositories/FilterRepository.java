package com.fmworkflow.workflow.domain.repositories;


import com.fmworkflow.workflow.domain.Filter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FilterRepository extends MongoRepository<Filter, String> {

    List<Filter> findByRolesIn(List<String> roles);
}
