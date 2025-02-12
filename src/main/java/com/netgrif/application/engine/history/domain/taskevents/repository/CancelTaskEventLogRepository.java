//package com.netgrif.application.engine.history.domain.taskevents.repository;
//
//
//import org.bson.types.ObjectId;
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//import com.netgrif.core.history.domain.taskevents.CancelTaskEventLog;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface CancelTaskEventLogRepository extends ElasticsearchRepository<CancelTaskEventLog, ObjectId> {
//
//    List<CancelTaskEventLog> findAllByTaskId(ObjectId taskId);
//
//    List<CancelTaskEventLog> findAllByUserId(String id);
//}
