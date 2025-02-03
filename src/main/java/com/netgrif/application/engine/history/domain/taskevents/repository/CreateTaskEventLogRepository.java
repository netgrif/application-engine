package com.netgrif.application.engine.history.domain.taskevents.repository;

import com.netgrif.application.engine.history.domain.taskevents.CreateTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreateTaskEventLogRepository extends ElasticsearchRepository<CreateTaskEventLog, ObjectId> {
}
