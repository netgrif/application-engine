package com.netgrif.workflow.elastic.domain.repository;

import com.netgrif.workflow.elastic.domain.ElasticTask;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticTaskRepository extends ElasticsearchRepository<ElasticTask, String> {

    ElasticTask findByStringId(String stringId);

    void deleteByStringId(String taskId);
}