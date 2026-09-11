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

    /**
     * Returns the resource type that this search service is designed to handle.
     * <p>
     * This method identifies the specific {@link QueryType} associated with the resource
     * managed by implementations of this service. It is used for validation to ensure
     * that query evaluators match the expected resource type.
     * </p>
     *
     * @return the {@link QueryType} representing the resource type handled by this service
     */
    QueryType getQueryResourceType();

    /**
     * Searches for a single resource using a query string with optional placeholder arguments.
     * <p>
     * The query string may contain placeholders that will be replaced with the provided arguments.
     * This method parses and evaluates the query string, then executes the search operation.
     * </p>
     *
     * @param queryString the query language expression to evaluate and execute
     * @param args        optional arguments to substitute into query placeholders
     * @return the first resource matching the query, or null if no resource is found
     * @throws IllegalArgumentException if the query is invalid or expects multiple results
     */
    Resource searchOne(String queryString, Object... args);

    /**
     * Searches for a single resource using a pre-evaluated query.
     * <p>
     * This method executes a search operation using a {@link QueryLangEvaluator} that has
     * already been evaluated and validated. The evaluator must be configured to expect
     * a single result and match the service's resource type.
     * </p>
     *
     * @param evaluator the evaluated query object containing the search criteria and metadata
     * @return the first resource matching the query, or null if no resource is found
     * @throws IllegalArgumentException if the evaluator is null, not configured for single results,
     *                                  or has a resource type mismatch
     */
    Resource searchOne(QueryLangEvaluator evaluator);

    /**
     * Searches for all resources matching a query string with pagination support and optional placeholder arguments.
     * <p>
     * The query string may contain placeholders that will be replaced with the provided arguments.
     * This method parses and evaluates the query string, then executes a paginated search operation.
     * Results are returned in pages according to the pagination settings in the query.
     * </p>
     *
     * @param queryString the query language expression to evaluate and execute
     * @param args        optional arguments to substitute into query placeholders
     * @return a page of resources matching the query with pagination information
     * @throws IllegalArgumentException if the query is invalid or expects a single result
     */
    Page<Resource> searchAll(String queryString, Object... args);

    /**
     * Searches for all resources matching a pre-evaluated query with pagination support.
     * <p>
     * This method executes a paginated search operation using a {@link QueryLangEvaluator} that has
     * already been evaluated and validated. The evaluator must be configured to expect
     * multiple results and match the service's resource type. Pagination settings from the
     * evaluator determine the page size and number.
     * </p>
     *
     * @param evaluator the evaluated query object containing the search criteria, pagination settings, and metadata
     * @return a page of resources matching the query with pagination information
     * @throws IllegalArgumentException if the evaluator is null, not configured for multiple results,
     *                                  or has a resource type mismatch
     */
    Page<Resource> searchAll(QueryLangEvaluator evaluator);

    /**
     * Counts the number of resources matching a query string with optional placeholder arguments.
     * <p>
     * The query string may contain placeholders that will be replaced with the provided arguments.
     * This method parses and evaluates the query string, then counts the matching resources
     * without retrieving them.
     * </p>
     *
     * @param queryString the query language expression to evaluate and execute
     * @param args        optional arguments to substitute into query placeholders
     * @return the number of resources matching the query
     * @throws IllegalArgumentException if the query is invalid
     */
    long count(String queryString, Object... args);

    /**
     * Counts the number of resources matching a pre-evaluated query.
     * <p>
     * This method executes a count operation using a {@link QueryLangEvaluator} that has
     * already been evaluated and validated. The evaluator must match the service's resource type.
     * This operation counts matching resources without retrieving them.
     * </p>
     *
     * @param evaluator the evaluated query object containing the search criteria and metadata
     * @return the number of resources matching the query
     * @throws IllegalArgumentException if the evaluator is null or has a resource type mismatch
     */
    long count(QueryLangEvaluator evaluator);

    /**
     * Checks if any resource exists that matches a query string with optional placeholder arguments.
     * <p>
     * The query string may contain placeholders that will be replaced with the provided arguments.
     * This method parses and evaluates the query string, then checks for the existence of at least
     * one matching resource without retrieving it.
     * </p>
     *
     * @param queryString the query language expression to evaluate and execute
     * @param args        optional arguments to substitute into query placeholders
     * @return true if at least one resource matching the query exists, false otherwise
     * @throws IllegalArgumentException if the query is invalid
     */
    boolean exists(String queryString, Object... args);

    /**
     * Checks if any resource exists that matches a pre-evaluated query.
     * <p>
     * This method executes an existence check using a {@link QueryLangEvaluator} that has
     * already been evaluated and validated. The evaluator must match the service's resource type.
     * This operation checks for the existence of at least one matching resource without retrieving it.
     * </p>
     *
     * @param evaluator the evaluated query object containing the search criteria and metadata
     * @return true if at least one resource matching the query exists, false otherwise
     * @throws IllegalArgumentException if the evaluator is null or has a resource type mismatch
     */
    boolean exists(QueryLangEvaluator evaluator);

    /**
     * Validates that the query evaluator is not null.
     *
     * @param evaluator the query evaluator to validate
     * @throws IllegalArgumentException if the evaluator is null
     */
    default void checkEvaluatorNotNull(QueryLangEvaluator evaluator) {
        if (evaluator == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
    }

    /**
     * Validates that the query evaluator expects multiple results.
     *
     * @param evaluator the query evaluator to validate
     * @throws IllegalArgumentException if the evaluator expects a single result
     */
    default void checkEvaluatorIsMultiple(QueryLangEvaluator evaluator) {
        if (!evaluator.getMultiple()) {
            throw new IllegalArgumentException("Cannot use searchAll() with a query that expects single result. Use searchOne() instead.");
        }
    }

    /**
     * Validates that the query evaluator expects a single result.
     *
     * @param evaluator the query evaluator to validate
     * @throws IllegalArgumentException if the evaluator expects multiple results
     */
    default void checkEvaluatorIsSingle(QueryLangEvaluator evaluator) {
        if (evaluator.getMultiple()) {
            throw new IllegalArgumentException("Cannot use searchOne() with a query that expects multiple results. Use searchAll() instead.");
        }
    }

    /**
     * Validates that the query evaluator's resource type matches the expected type.
     *
     * @param evaluator the query evaluator to validate
     * @throws IllegalArgumentException if the resource type does not match
     */
    default void checkEvaluatorResourceType(QueryLangEvaluator evaluator) {
        if (evaluator.getResourceType() != getQueryResourceType()) {
            throw new IllegalArgumentException(String.format("Wrong query resource type. Should be: %s, was: %s",
                    getQueryResourceType(), evaluator.getResourceType()));
        }
    }
}
