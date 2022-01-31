package com.netgrif.application.engine.workflow.domain.repositories;


import com.netgrif.application.engine.workflow.domain.Filter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * @deprecated
 * since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
@Repository
public interface FilterRepository extends MongoRepository<Filter, String>, QuerydslPredicateExecutor<Filter> {
}