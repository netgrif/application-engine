package com.netgrif.workflow.elastic.domain.repository;

import com.netgrif.workflow.elastic.domain.mapping.TextField;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticTextDataRepository extends ElasticsearchRepository<TextField, String> {

//    TextField findBy
}
