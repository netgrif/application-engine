package com.netgrif.application.engine.pfql.service.userresource;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.IResourceSearchService;
import com.netgrif.application.engine.pfql.service.QueryLangEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.evaluateQuery;

/**
 * Service for searching and querying user resources using PFQL (Process Flow Query Language).
 * <p>
 * This service provides methods to search for users, count users, and check user existence
 * based on PFQL query strings or evaluated query objects. It delegates the actual MongoDB
 * queries to the {@link IUserService}.
 * </p>
 *
 * @see IResourceSearchService
 * @see IUserService
 * @see QueryLangEvaluator
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchService implements IResourceSearchService<IUser> {

    public static final String QUERY_SINGLE_PREFIX = "user: ";
    public static final String QUERY_MULTIPLE_PREFIX = "users: ";

    private final IUserService userService;

    /**
     * Returns the resource type handled by this search service.
     *
     * @return {@link QueryType#USER} indicating this service handles user resources
     */
    @Override
    public QueryType getQueryResourceType() {
        return QueryType.USER;
    }

    /**
     * Searches for a single user using a PFQL query string.
     *
     * @param queryString the PFQL query string to search with (e.g., "user: email == 'user@example.com'")
     * @return the first user matching the query, or null if no user is found
     * @throws IllegalArgumentException if the query string is invalid or evaluates to a non-USER resource type
     */
    @Override
    public IUser searchOne(String queryString) {
        log.debug("Searching for single user with query: {}", queryString);
        return searchOne(evaluateQuery(queryString));
    }

    /**
     * Searches for a single user using a pre-evaluated query.
     *
     * @param evaluator the evaluated query object containing the MongoDB query and metadata
     * @return the first user matching the query, or null if no user is found
     * @throws IllegalArgumentException if the evaluator is null, not configured for single results,
     *                                  or has a resource type other than USER
     */
    @Override
    public IUser searchOne(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorIsSingle(evaluator);
        checkEvaluatorResourceType(evaluator);

        log.debug("Searching for single user using MongoDB");
        log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
        IUser result = userService.searchOne(evaluator.getFullMongoQuery());
        log.trace("MongoDB search one result: {}", result != null ? result.getStringId() : "null");
        return result;
    }

    /**
     * Searches for all users matching a PFQL query string with pagination support.
     *
     * @param queryString the PFQL query string to search with (e.g., "users: email like '%@example.com'")
     * @return a page of users matching the query
     * @throws IllegalArgumentException if the query string is invalid or evaluates to a non-USER resource type
     */
    @Override
    public Page<IUser> searchAll(String queryString) {
        log.debug("Searching for all users with query: {}", queryString);
        return searchAll(evaluateQuery(queryString));
    }

    /**
     * Searches for all users matching a pre-evaluated query with pagination support.
     *
     * @param evaluator the evaluated query object containing the MongoDB query, pagination settings, and metadata
     * @return a page of users matching the query with pagination information
     * @throws IllegalArgumentException if the evaluator is null, not configured for multiple results,
     *                                  or has a resource type other than USER
     */
    @Override
    public Page<IUser> searchAll(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorIsMultiple(evaluator);
        checkEvaluatorResourceType(evaluator);

        log.debug("Searching for all users using MongoDB with pagination: page={}, size={}",
                evaluator.getPageable().getPageNumber(), evaluator.getPageable().getPageSize());
        log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
        Page<IUser> result = userService.search(evaluator.getFullMongoQuery(), evaluator.getPageable());
        log.trace("MongoDB search all result: page size={}, total elements={}", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    /**
     * Counts the number of users matching a PFQL query string.
     *
     * @param queryString the PFQL query string to count with (e.g., "users: email like '%@example.com'")
     * @return the number of users matching the query
     * @throws IllegalArgumentException if the query string is invalid or evaluates to a non-USER resource type
     */
    @Override
    public long count(String queryString) {
        log.debug("Counting users with query: {}", queryString);
        return count(evaluateQuery(queryString));
    }

    /**
     * Counts the number of users matching a pre-evaluated query.
     *
     * @param evaluator the evaluated query object containing the MongoDB query and metadata
     * @return the number of users matching the query
     * @throws IllegalArgumentException if the evaluator is null or has a resource type other than USER
     */
    @Override
    public long count(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorResourceType(evaluator);

        log.debug("Counting users using MongoDB");
        log.trace("Executing MongoDB count query: {}", evaluator.getFullMongoQuery());
        long result = userService.count(evaluator.getFullMongoQuery());
        log.trace("MongoDB count result: {}", result);
        return result;
    }

    /**
     * Checks if any user exists that matches a PFQL query string.
     *
     * @param queryString the PFQL query string to check with (e.g., "user: email == 'user@example.com'")
     * @return true if at least one user matching the query exists, false otherwise
     * @throws IllegalArgumentException if the query string is invalid or evaluates to a non-USER resource type
     */
    @Override
    public boolean exists(String queryString) {
        log.debug("Checking existence of user with query: {}", queryString);
        return exists(evaluateQuery(queryString));
    }

    /**
     * Checks if any user exists that matches a pre-evaluated query.
     *
     * @param evaluator the evaluated query object containing the MongoDB query and metadata
     * @return true if at least one user matching the query exists, false otherwise
     * @throws IllegalArgumentException if the evaluator is null or has a resource type other than USER
     */
    @Override
    public boolean exists(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorResourceType(evaluator);

        log.debug("Checking existence of users using MongoDB");
        log.trace("Executing MongoDB exists query: {}", evaluator.getFullMongoQuery());
        boolean result = userService.exists(evaluator.getFullMongoQuery());
        log.trace("MongoDB exists result: {}", result);
        return result;
    }
}
