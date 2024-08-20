package com.netgrif.application.engine.search.interfaces;

public interface ISearchService {

    Object search(String query);

    Long count(String query);
}
