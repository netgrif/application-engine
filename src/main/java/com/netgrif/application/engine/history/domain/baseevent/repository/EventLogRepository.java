package com.netgrif.application.engine.history.domain.baseevent.repository;

import com.netgrif.application.engine.history.domain.baseevent.EventLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends ElasticsearchRepository<EventLog, String> {

    List<EventLog> findAllById(List<String> eventIds);

}
