package com.netgrif.application.engine.history.domain.dataevents.repository;

import com.netgrif.application.engine.history.domain.dataevents.SetDataEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(since = "6.3.0")
public interface SetDataEventLogRepository extends MongoRepository<SetDataEventLog, ObjectId> {

    List<SetDataEventLog> findAllByCaseId(ObjectId caseId);

    List<SetDataEventLog> findAllByTaskId(ObjectId taskId);
}
