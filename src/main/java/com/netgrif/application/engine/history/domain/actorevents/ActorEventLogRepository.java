package com.netgrif.application.engine.history.domain.actorevents;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorEventLogRepository extends MongoRepository<IActorEventLog, ObjectId> {

}
