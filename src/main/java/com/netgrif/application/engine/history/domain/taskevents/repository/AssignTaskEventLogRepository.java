package com.netgrif.application.engine.history.domain.taskevents.repository;

import com.netgrif.application.engine.history.domain.taskevents.AssignTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(since = "6.3.0")
public interface AssignTaskEventLogRepository extends MongoRepository<AssignTaskEventLog, ObjectId> {

    List<AssignTaskEventLog> findAllByTaskId(ObjectId taskId);

    List<AssignTaskEventLog> findAllByUserId(String id);

    List<AssignTaskEventLog> findAllByCaseId(String caseId);
}
