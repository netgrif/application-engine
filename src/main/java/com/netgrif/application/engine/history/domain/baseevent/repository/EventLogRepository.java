package com.netgrif.application.engine.history.domain.baseevent.repository;

import com.netgrif.core.history.domain.baseevent.EventLog;
import org.bson.types.ObjectId;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends ElasticsearchRepository<EventLog, ObjectId> {

    List<EventLog> findAllById(List<ObjectId> eventIds);
}
