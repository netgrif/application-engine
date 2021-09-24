package com.netgrif.workflow.history.domain.taskevents.repository;

import com.netgrif.workflow.history.domain.taskevents.DelegateTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DelegateTaskEventLogRepository extends MongoRepository<DelegateTaskEventLog, ObjectId> {

    List<DelegateTaskEventLog> findAllByTaskId(ObjectId taskId);

    List<DelegateTaskEventLog> findAllByTaskIdAndDelegatee(ObjectId taskId, Long delegateeId);
}
