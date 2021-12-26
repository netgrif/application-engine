package com.netgrif.workflow.rules.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RuleRepository extends MongoRepository<StoredRule, ObjectId>, QuerydslPredicateExecutor<StoredRule> {

    boolean existsByLastUpdateAfter(LocalDateTime time);

    StoredRule findByIdentifier(String identifier);

    List<StoredRule> findByIdentifierIn(List<String> identifiers);
}