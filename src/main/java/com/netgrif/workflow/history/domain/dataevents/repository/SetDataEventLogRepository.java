package com.netgrif.workflow.history.domain.dataevents.repository;

import com.netgrif.workflow.history.domain.dataevents.SetDataEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetDataEventLogRepository extends MongoRepository<SetDataEventLog, ObjectId> {

    List<SetDataEventLog> findAllByCaseId(ObjectId caseId);

    List<SetDataEventLog> findAllByTaskId(ObjectId taskId);
}
