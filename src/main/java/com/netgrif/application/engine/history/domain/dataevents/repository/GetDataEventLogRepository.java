package com.netgrif.application.engine.history.domain.dataevents.repository;

import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.history.domain.dataevents.GetDataEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GetDataEventLogRepository extends ElasticsearchRepository<GetDataEventLog, ObjectId> {

    List<GetDataEventLog> findAllByCaseId(ObjectId caseId);

    List<GetDataEventLog> findAllByTaskId(ObjectId taskId);
}
