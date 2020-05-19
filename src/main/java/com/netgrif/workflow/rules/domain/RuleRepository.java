package com.netgrif.workflow.rules.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface RuleRepository extends MongoRepository<StoredRule, ObjectId> {

    boolean existsByLastUpdateAfter(LocalDateTime time);
}