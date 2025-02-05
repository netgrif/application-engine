package com.netgrif.application.engine.history.domain.dataevents.repository;

import com.netgrif.core.history.domain.dataevents.SetDataEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetDataEventLogRepository extends ElasticsearchRepository<SetDataEventLog, ObjectId> {

    List<SetDataEventLog> findAllByCaseId(ObjectId caseId);

    List<SetDataEventLog> findAllByTaskId(ObjectId taskId);
}
