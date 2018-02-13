package com.netgrif.workflow.workflow.domain.repositories;


import com.netgrif.workflow.workflow.domain.Filter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FilterRepository extends MongoRepository<Filter, String> {

}
