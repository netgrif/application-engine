package com.netgrif.application.engine.history.domain.userevents;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEventLogRepository extends MongoRepository<IUserEventLog, ObjectId> {

}
