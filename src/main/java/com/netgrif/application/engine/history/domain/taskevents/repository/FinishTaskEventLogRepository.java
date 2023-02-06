package com.netgrif.application.engine.history.domain.taskevents.repository;

import com.netgrif.application.engine.history.domain.taskevents.FinishTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(since = "6.3.0")
public interface FinishTaskEventLogRepository extends MongoRepository<FinishTaskEventLog, ObjectId> {

    List<FinishTaskEventLog> findAllByTaskId(ObjectId taskId);

    List<FinishTaskEventLog> findAllByUserId(String id);

    List<FinishTaskEventLog> findAllByCaseId(String caseId);
}
