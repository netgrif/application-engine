package com.netgrif.application.engine.elastic.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticPetriNetRepository extends ElasticsearchRepository<ElasticPetriNet, String> {

    ElasticPetriNet findByStringId(String stringId);

    List<ElasticPetriNet> findAllByUriNodeId(String uriNodeId);

    void deleteAllByStringId(String id);

    List<ElasticPetriNet> findAllByIdentifier(String identifier);
}
