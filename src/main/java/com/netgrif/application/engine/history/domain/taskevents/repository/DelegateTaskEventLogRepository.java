package com.netgrif.application.engine.history.domain.taskevents.repository;

import com.netgrif.core.history.domain.taskevents.DelegateTaskEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DelegateTaskEventLogRepository extends ElasticsearchRepository<DelegateTaskEventLog, ObjectId> {

    List<DelegateTaskEventLog> findAllByTaskId(ObjectId taskId);

    List<DelegateTaskEventLog> findAllByTaskIdAndDelegate(ObjectId taskId, Long delegateId);
}
