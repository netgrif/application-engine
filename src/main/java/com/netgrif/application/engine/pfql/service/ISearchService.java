package com.netgrif.application.engine.pfql.service;

public interface ISearchService {
    String explainQuery(String query);

    Object search(String query);

    Long count(String query);

    boolean exists(String query);
}
