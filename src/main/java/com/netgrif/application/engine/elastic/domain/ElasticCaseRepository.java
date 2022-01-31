package com.netgrif.application.engine.elastic.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticCaseRepository extends ElasticsearchRepository<ElasticCase, String> {

    ElasticCase findByStringId(String stringId);

    long countByStringIdAndLastModified(String stringId, long lastUpdated);

    void deleteAllByStringId(String id);

    void deleteAllByProcessId(String processId);
}