package com.netgrif.application.engine.history.domain.taskevents.repository;

import com.netgrif.application.engine.history.domain.taskevents.DelegateTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(since = "6.3.0")
public interface DelegateTaskEventLogRepository extends MongoRepository<DelegateTaskEventLog, ObjectId> {

    List<DelegateTaskEventLog> findAllByTaskId(ObjectId taskId);

    List<DelegateTaskEventLog> findAllByTaskIdAndDelegate(ObjectId taskId, Long delegateId);
}
