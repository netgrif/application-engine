package com.netgrif.workflow.history.domain.repository;

import com.netgrif.workflow.history.domain.CaseSaveDataEventLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseSaveDataEventLogRepository extends MongoRepository<CaseSaveDataEventLog, ObjectId>, QueryDslPredicateExecutor<CaseSaveDataEventLog> {
}