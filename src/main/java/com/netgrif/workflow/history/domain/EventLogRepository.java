package com.netgrif.workflow.history.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogRepository extends MongoRepository<EventLog, ObjectId> {
}
