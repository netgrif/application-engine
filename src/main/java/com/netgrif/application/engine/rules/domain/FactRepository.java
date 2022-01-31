package com.netgrif.application.engine.rules.domain;

import com.netgrif.application.engine.rules.domain.facts.Fact;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface FactRepository extends MongoRepository<Fact, ObjectId>, QuerydslPredicateExecutor<Fact> {

}