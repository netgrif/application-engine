package com.netgrif.application.engine.elastic.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet;

/**
 * Repository interface for managing {@link ElasticPetriNet} entities in Elasticsearch.
 * Extends {@link ElasticsearchRepository} to provide CRUD operations and additional query methods.
 */
@Repository
public interface ElasticPetriNetRepository extends ElasticsearchRepository<ElasticPetriNet, String> {

    /**
     * Deletes all {@link ElasticPetriNet} entities with the given string ID.
     *
     * @param id the string ID of the {@link ElasticPetriNet} entities to delete
     */
    void deleteAllById(String id);
}