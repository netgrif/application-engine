package com.netgrif.workflow.history.domain.dataevents.repository;

import com.netgrif.workflow.history.domain.dataevents.GetDataEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GetDataEventLogRepository extends MongoRepository<GetDataEventLog, ObjectId> {

    List<GetDataEventLog> findAllByCaseId(ObjectId caseId);

    List<GetDataEventLog> findAllByTaskId(ObjectId taskId);
}
