package com.netgrif.application.engine.history.domain.taskevents.repository;

import com.netgrif.application.engine.history.domain.taskevents.CancelTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CancelTaskEventLogRepository extends MongoRepository<CancelTaskEventLog, ObjectId> {

    List<CancelTaskEventLog> findAllByTaskId(ObjectId taskId);

    List<CancelTaskEventLog> findAllByUserId(String id);
}
