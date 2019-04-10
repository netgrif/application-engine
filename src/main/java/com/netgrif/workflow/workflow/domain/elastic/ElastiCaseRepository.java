package com.netgrif.workflow.workflow.domain.elastic;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElastiCaseRepository extends ElasticsearchRepository<ElastiCase, String> {
}
