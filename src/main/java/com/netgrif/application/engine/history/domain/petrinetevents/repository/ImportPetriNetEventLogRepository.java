package com.netgrif.application.engine.history.domain.petrinetevents.repository;

import com.netgrif.application.engine.history.domain.petrinetevents.ImportPetriNetEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(since = "6.3.0")
public interface ImportPetriNetEventLogRepository extends MongoRepository<ImportPetriNetEventLog, ObjectId> {

    List<ImportPetriNetEventLog> findAllByNetId(ObjectId netId);
}
