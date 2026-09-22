package com.netgrif.application.engine.pfql.service.userresource;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.pfql.domain.antlr4.QueryLangParser;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.AbstractResourceSearchService;
import com.netgrif.application.engine.pfql.service.QueryLangEvaluator;
import com.netgrif.application.engine.pfql.service.formatters.QueryLangPlaceholderHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * Service for searching and querying user resources using PFQL (Process Flow Query Language).
 * <p>
 * This service provides methods to search for users, count users, and check user existence
 * based on PFQL query strings or evaluated query objects. It delegates the actual MongoDB
 * queries to the {@link IUserService}.
 * </p>
 */
@Slf4j
@Service
public class UserSearchService extends AbstractResourceSearchService<IUser> {

    protected final IUserService userService;

    public UserSearchService(QueryLangPlaceholderHandler placeholderHandler, IUserService userService) {
        super(placeholderHandler);
        this.userService = userService;
    }

    /**
     * Returns the resource type handled by this search service.
     *
     * @return {@link QueryType#USER} indicating this service handles user resources
     */
    @Override
    public QueryType getQueryResourceType() {
        return QueryType.USER;
    }

    @Override
    protected String ensurePrefix(String query, boolean isMulti) {
        return doEnsurePrefix(query, isMulti, QueryLangParser.USERS, QueryLangParser.USER);
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
    protected IUser doSearchOne(QueryLangEvaluator evaluator) {
        log.debug("Searching for single user using MongoDB");
        log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
        IUser result = userService.searchOne(evaluator.getFullMongoQuery());
        log.trace("MongoDB search one result: {}", result != null ? result.getStringId() : "null");
        return result;
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
    protected Page<IUser> doSearchAll(QueryLangEvaluator evaluator) {
        log.debug("Searching for all users using MongoDB with pagination: page={}, size={}",
                evaluator.getPageable().getPageNumber(), evaluator.getPageable().getPageSize());
        log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
        Page<IUser> result = userService.search(evaluator.getFullMongoQuery(), evaluator.getPageable());
        log.trace("MongoDB search all result: page size={}, total elements={}", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    /**
     * Counts the number of users matching a pre-evaluated query.
     *
     * @param evaluator the evaluated query object containing the MongoDB query and metadata
     * @return the number of users matching the query
     * @throws IllegalArgumentException if the evaluator is null or has a resource type other than USER
     */
    @Override
    protected long doCount(QueryLangEvaluator evaluator) {
        log.debug("Counting users using MongoDB");
        log.trace("Executing MongoDB count query: {}", evaluator.getFullMongoQuery());
        long result = userService.count(evaluator.getFullMongoQuery());
        log.trace("MongoDB count result: {}", result);
        return result;
    }

    /**
     * Checks if any user exists that matches a pre-evaluated query.
     *
     * @param evaluator the evaluated query object containing the MongoDB query and metadata
     * @return true if at least one user matching the query exists, false otherwise
     * @throws IllegalArgumentException if the evaluator is null or has a resource type other than USER
     */
    @Override
    protected boolean doExists(QueryLangEvaluator evaluator) {
        log.debug("Checking existence of users using MongoDB");
        log.trace("Executing MongoDB exists query: {}", evaluator.getFullMongoQuery());
        boolean result = userService.exists(evaluator.getFullMongoQuery());
        log.trace("MongoDB exists result: {}", result);
        return result;
    }
}
