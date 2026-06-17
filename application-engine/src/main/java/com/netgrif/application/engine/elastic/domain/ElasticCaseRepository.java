package com.netgrif.application.engine.elastic.domain;

import com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticCase;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticCaseRepository extends ElasticsearchRepository<ElasticCase, String> {

    long countByIdAndLastModified(String stringId, long lastUpdated);

    void deleteAllById(String id);

    void deleteAllByProcessId(String processId);
}
