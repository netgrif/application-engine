package com.netgrif.workflow.history.domain;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEventLogRepository extends MongoRepository<UserEventLog, ObjectId> {

    Page<UserEventLog> findAllByEmail(Pageable pageable,String email);
}
