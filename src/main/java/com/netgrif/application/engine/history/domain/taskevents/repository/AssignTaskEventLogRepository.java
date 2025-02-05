package com.netgrif.application.engine.history.domain.taskevents.repository;

import com.netgrif.core.history.domain.taskevents.AssignTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignTaskEventLogRepository extends ElasticsearchRepository<AssignTaskEventLog, ObjectId> {

    List<AssignTaskEventLog> findAllByTaskId(ObjectId taskId);

    List<AssignTaskEventLog> findAllByUserId(String id);

    List<AssignTaskEventLog> findAllByCaseId(String caseId);
}
