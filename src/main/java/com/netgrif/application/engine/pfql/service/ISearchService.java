package com.netgrif.application.engine.pfql.service;

public interface ISearchService {
    String explainQuery(String query, Object... args);

    Object search(String query, Object... args);

    long count(String query, Object... args);

    boolean exists(String query, Object... args);
}
