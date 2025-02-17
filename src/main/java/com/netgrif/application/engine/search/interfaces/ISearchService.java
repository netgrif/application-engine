package com.netgrif.application.engine.search.interfaces;

public interface ISearchService {
    String explainQuery(String query);

    Object search(String query);

    Long count(String query);
}
