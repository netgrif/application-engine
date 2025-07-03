package com.netgrif.application.engine.elastic.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet;

import java.util.List;

/**
 * Repository interface for managing {@link ElasticPetriNet} entities in Elasticsearch.
 * Extends {@link ElasticsearchRepository} to provide CRUD operations and additional query methods.
 */
@Repository
public interface ElasticPetriNetRepository extends ElasticsearchRepository<ElasticPetriNet, String> {

    /**
     * Finds an {@link ElasticPetriNet} entity by its string ID.
     *
     * @param stringId the string ID of the {@link ElasticPetriNet} to find
     * @return the {@link ElasticPetriNet} entity with the given string ID, or {@code null} if none found
     */
    ElasticPetriNet findByStringId(String stringId);

    /**
     * Deletes all {@link ElasticPetriNet} entities with the given string ID.
     *
     * @param id the string ID of the {@link ElasticPetriNet} entities to delete
     */
    void deleteAllByStringId(String id);
}