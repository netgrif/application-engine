package com.netgrif.application.engine.elastic.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.netgrif.adapter.elastic.domain.ElasticTask;

import java.util.List;

@Repository
public interface ElasticTaskRepository extends ElasticsearchRepository<ElasticTask, String> {

    ElasticTask findByStringId(String stringId);

    ElasticTask findByTaskId(String taskId);

    void deleteAllByStringId(String taskId);

    ElasticTask deleteAllByTaskId(String taskId);

    void deleteAllByProcessId(String processId);

    List<ElasticTask> findAllByProcessId(String processId);
}