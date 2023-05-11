package com.netgrif.application.engine.history.domain.baseevent.repository;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends MongoRepository<EventLog, ObjectId> {

    List<EventLog> findAllById(List<ObjectId> eventIds);
}
