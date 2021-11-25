package com.netgrif.workflow.history.domain.taskevents.repository;

import com.netgrif.workflow.history.domain.taskevents.FinishTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FinishTaskEventLogRepository extends MongoRepository<FinishTaskEventLog, ObjectId> {

    List<FinishTaskEventLog> findAllByTaskId(ObjectId taskId);

    List<FinishTaskEventLog> findAllByUserId(Long id);
}
