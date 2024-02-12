package com.netgrif.application.engine.history.domain.taskevents.repository;

import com.netgrif.application.engine.history.domain.taskevents.CancelTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(since = "6.3.0")
public interface CancelTaskEventLogRepository extends MongoRepository<CancelTaskEventLog, ObjectId> {

    List<CancelTaskEventLog> findAllByTaskId(ObjectId taskId);

    List<CancelTaskEventLog> findAllByUserId(String id);
}
