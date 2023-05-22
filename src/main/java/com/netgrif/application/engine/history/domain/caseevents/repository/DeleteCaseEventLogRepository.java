package com.netgrif.application.engine.history.domain.caseevents.repository;

import com.netgrif.application.engine.history.domain.caseevents.DeleteCaseEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(since = "6.3.0")
public interface DeleteCaseEventLogRepository extends MongoRepository<DeleteCaseEventLog, ObjectId> {

    List<DeleteCaseEventLog> findAllByCaseId(ObjectId caseId);
}
