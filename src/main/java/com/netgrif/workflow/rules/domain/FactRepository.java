package com.netgrif.workflow.rules.domain;

import com.netgrif.workflow.history.domain.EventLog;
import com.netgrif.workflow.rules.domain.facts.Fact;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface FactRepository extends MongoRepository<Fact, ObjectId>, QuerydslPredicateExecutor<Fact> {

}