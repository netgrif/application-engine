package com.netgrif.workflow.history.domain.taskevents.repository;

import com.netgrif.workflow.history.domain.taskevents.CancelTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CancelTaskEventLogRepository extends MongoRepository<CancelTaskEventLog, ObjectId> {

    List<CancelTaskEventLog> findAllByTaskId(ObjectId taskId);

    List<CancelTaskEventLog> findAllByUserId(Long id);
}
