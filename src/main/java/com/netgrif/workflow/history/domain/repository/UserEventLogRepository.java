package com.netgrif.workflow.history.domain.repository;

import com.netgrif.workflow.history.domain.IUserEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEventLogRepository extends MongoRepository<IUserEventLog, ObjectId> {

    Page<IUserEventLog> findAllByEmail(Pageable pageable, String email);
}
