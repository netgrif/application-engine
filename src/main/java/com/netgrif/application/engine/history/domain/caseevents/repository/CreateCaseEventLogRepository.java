package com.netgrif.application.engine.history.domain.caseevents.repository;

import com.netgrif.application.engine.history.domain.caseevents.CreateCaseEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreateCaseEventLogRepository extends ElasticsearchRepository<CreateCaseEventLog, ObjectId> {

    List<CreateCaseEventLog> findAllByCaseId(ObjectId caseId);
}
