package com.netgrif.workflow.history.domain.caseevents.repository;

import com.netgrif.workflow.history.domain.caseevents.CreateCaseEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreateCaseEventLogRepository extends MongoRepository<CreateCaseEventLog, ObjectId> {

    List<CreateCaseEventLog> findAllByCaseId(ObjectId caseId);
}
