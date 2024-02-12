package com.netgrif.application.engine.history.domain.dataevents.repository;

import com.netgrif.application.engine.history.domain.dataevents.GetDataEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(since = "6.3.0")
public interface GetDataEventLogRepository extends MongoRepository<GetDataEventLog, ObjectId> {

    List<GetDataEventLog> findAllByCaseId(ObjectId caseId);

    List<GetDataEventLog> findAllByTaskId(ObjectId taskId);
}
