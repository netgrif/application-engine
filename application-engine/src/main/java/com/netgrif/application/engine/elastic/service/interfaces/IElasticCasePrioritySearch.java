package com.netgrif.application.engine.elastic.service.interfaces;


import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;

import java.util.List;
import java.util.Map;

public interface IElasticCasePrioritySearch {

    /**
     * See {@link QueryStringQuery.Builder#fields(List)}
     *
     * @return list where members are ElasticCase field with boost separated by "^" of these fields, e.g. ["stringId^2"]
     */
    List<String> fullTextFields();
}
