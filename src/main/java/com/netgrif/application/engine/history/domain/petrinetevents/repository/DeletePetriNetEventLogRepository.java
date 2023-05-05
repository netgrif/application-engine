package com.netgrif.application.engine.history.domain.petrinetevents.repository;

import com.netgrif.application.engine.history.domain.petrinetevents.DeletePetriNetEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated(since = "6.2.2")
public interface DeletePetriNetEventLogRepository extends MongoRepository<DeletePetriNetEventLog, ObjectId> {

    List<DeletePetriNetEventLog> findAllByNetId(ObjectId netId);
}
