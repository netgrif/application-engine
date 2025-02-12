//package com.netgrif.application.engine.history.domain.petrinetevents.repository;
//
//import com.netgrif.core.history.domain.petrinetevents.ImportPetriNetEventLog;
//import org.bson.types.ObjectId;
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface ImportPetriNetEventLogRepository extends ElasticsearchRepository<ImportPetriNetEventLog, ObjectId> {
//
//    List<ImportPetriNetEventLog> findAllByNetId(ObjectId netId);
//}
