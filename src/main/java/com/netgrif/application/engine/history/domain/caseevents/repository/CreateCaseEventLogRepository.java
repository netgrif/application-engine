package com.netgrif.application.engine.history.domain.caseevents.repository;

import com.netgrif.application.engine.history.domain.caseevents.CreateCaseEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(since = "6.3.0")
public interface CreateCaseEventLogRepository extends MongoRepository<CreateCaseEventLog, ObjectId> {

    List<CreateCaseEventLog> findAllByCaseId(ObjectId caseId);
}
