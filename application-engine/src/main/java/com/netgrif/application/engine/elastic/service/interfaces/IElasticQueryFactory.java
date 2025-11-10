package com.netgrif.application.engine.elastic.service.interfaces;

import java.util.Map;

public interface IElasticQueryFactory {
    String populateQuery(String query, Map<String, Object> queryContext);
}
