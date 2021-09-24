package com.netgrif.workflow.history.domain.baseevent.repository;

import com.netgrif.workflow.history.domain.baseevent.EventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends MongoRepository<EventLog, ObjectId>, QuerydslPredicateExecutor<EventLog> {

    List<EventLog> findAllById(List<ObjectId> eventIds);
}
