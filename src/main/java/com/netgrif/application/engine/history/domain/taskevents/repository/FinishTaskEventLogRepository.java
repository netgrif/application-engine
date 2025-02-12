//package com.netgrif.application.engine.history.domain.taskevents.repository;
//
//import com.netgrif.core.history.domain.taskevents.FinishTaskEventLog;
//import org.bson.types.ObjectId;
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface FinishTaskEventLogRepository extends ElasticsearchRepository<FinishTaskEventLog, ObjectId> {
//
//    List<FinishTaskEventLog> findAllByTaskId(ObjectId taskId);
//
//    List<FinishTaskEventLog> findAllByUserId(String id);
//
//    List<FinishTaskEventLog> findAllByCaseId(String caseId);
//}
