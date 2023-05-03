package com.netgrif.application.engine.elastic.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticPetriNetRepository extends ElasticsearchRepository<ElasticPetriNet, String> {

    ElasticPetriNet findByStringId(String stringId);

    void deleteAllByStringId(String id);
}
