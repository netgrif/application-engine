package com.netgrif.workflow.history.domain.petrinetevents.repository;

import com.netgrif.workflow.history.domain.petrinetevents.ImportPetriNetEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ImportPetriNetEventLogRepository extends MongoRepository<ImportPetriNetEventLog, ObjectId> {

    List<ImportPetriNetEventLog> findAllByNetId(ObjectId netId);
}
