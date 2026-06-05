package com.netgrif.application.engine.pfql.service;


import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


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

    QueryType getQueryResourceType();

    Resource searchOne(String queryString);
    Resource searchOne(QueryLangEvaluator evaluator);

    Page<Resource> searchAll(String queryString);
    Page<Resource> searchAll(QueryLangEvaluator evaluator);

    long count(String queryString);
    long count(QueryLangEvaluator evaluator);

    boolean exists(String queryString);
    boolean exists(QueryLangEvaluator evaluator);

    static Pageable getDefaultPageable() {
        return PageRequest.ofSize(100);
    }

    // todo 2443 javadoc
    default void checkEvaluatorNotNull(QueryLangEvaluator evaluator) {
        if (evaluator == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
    }

    // todo 2443 javadoc
    default void updateWithDefaultPageableIfMissing(QueryLangEvaluator evaluator, Logger logger) {
        if (evaluator.getPageable() == null) {
            Pageable pageable = getDefaultPageable();
            logger.debug("Pageable was missing. Using default pageable: {}", pageable);
            evaluator.setPageable(getDefaultPageable());
        }
    }

    // todo 2443 javadoc
    default void checkEvaluatorMultiplicity(QueryLangEvaluator evaluator) {
        if (evaluator.getMultiple()) {
            throw new IllegalArgumentException("Cannot use searchOne() with a query that expects multiple results. Use searchAll() instead.");
        }
    }

    // todo 2443 javadoc
    default void checkEvaluatorResourceType(QueryLangEvaluator evaluator) {
        if (evaluator.getResourceType() != getQueryResourceType()) {
            throw new IllegalArgumentException(String.format("Wrong query resource type. Should be: %s, was: %s",
                    getQueryResourceType(), evaluator.getResourceType()));
        }
    }
}
