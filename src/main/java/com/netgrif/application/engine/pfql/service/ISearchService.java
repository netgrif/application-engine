package com.netgrif.application.engine.pfql.service;

public interface ISearchService {
    String explainQuery(String query);

    Object search(String query);

    long count(String query);

    boolean exists(String query);
}
