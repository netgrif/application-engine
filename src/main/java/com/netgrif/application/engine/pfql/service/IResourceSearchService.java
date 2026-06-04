package com.netgrif.application.engine.pfql.service;


import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import org.springframework.data.domain.Page;

/**
 * Service interface for searching resources using query language expressions.
 * <p>
 * Provides a unified contract for executing search operations on specific resource types
 * using both string-based queries and pre-evaluated query expressions. Implementations
 * of this interface should handle the translation of query language syntax into the
 * appropriate search mechanism (e.g., MongoDB queries, Elasticsearch queries).
 * </p>
 * <p>
 * This interface supports various search operations including single result retrieval,
 * paginated searches, counting matches, and existence checks. Each operation can be
 * performed using either a raw query string or a pre-evaluated {@link QueryLangEvaluator}.
 * </p>
 *
 * @param <Resource> the type of resource this service searches for (e.g., Case, Task, User)
 */
public interface IResourceSearchService<Resource> {

    QueryType getQueryType();

    Resource searchOne(String queryString);
    Resource searchOne(QueryLangEvaluator evaluator);

    Page<Resource> searchAll(String queryString);
    Page<Resource> searchAll(QueryLangEvaluator evaluator);

    long count(String queryString);
    long count(QueryLangEvaluator evaluator);

    boolean exists(String queryString);
    boolean exists(QueryLangEvaluator evaluator);
}
