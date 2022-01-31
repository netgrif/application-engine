package com.netgrif.application.engine.elastic.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticTaskRepository extends ElasticsearchRepository<ElasticTask, String> {

    ElasticTask findByStringId(String stringId);

    void deleteAllByStringId(String taskId);

    void deleteAllByProcessId(String processId);
}