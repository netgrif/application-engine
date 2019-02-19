package com.netgrif.workflow.history.domain.repository;

import com.netgrif.workflow.history.domain.EventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogRepository extends MongoRepository<EventLog, ObjectId>, QueryDslPredicateExecutor<EventLog> {
}
