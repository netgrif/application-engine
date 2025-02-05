package com.netgrif.application.engine.history.domain.caseevents.repository;

import com.netgrif.core.history.domain.caseevents.DeleteCaseEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeleteCaseEventLogRepository extends ElasticsearchRepository<DeleteCaseEventLog, ObjectId> {

    List<DeleteCaseEventLog> findAllByCaseId(ObjectId caseId);
}
