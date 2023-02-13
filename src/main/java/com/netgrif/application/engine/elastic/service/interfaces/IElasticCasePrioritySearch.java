package com.netgrif.application.engine.elastic.service.interfaces;

import org.elasticsearch.index.query.QueryStringQueryBuilder;

import java.util.Map;

public interface IElasticCasePrioritySearch {

    /**
     * See {@link QueryStringQueryBuilder#fields(Map)}
     *
     * @return map where keys are ElasticCase field names and values are boosts of these fields
     */
    Map<String, Float> fullTextFields();
}
