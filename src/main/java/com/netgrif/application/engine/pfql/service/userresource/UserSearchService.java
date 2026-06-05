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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchService implements IResourceSearchService<IUser> {

    private final IUserService userService;
    // todo 2443 javadoc

    @Override
    public QueryType getQueryResourceType() {
        return QueryType.USER;
    }

    @Override
    public IUser searchOne(String queryString) {
        log.debug("Searching for single user with query: {}", queryString);
        return searchOne(evaluateQuery(queryString));
    }

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

    @Override
    public Page<IUser> searchAll(String queryString) {
        log.debug("Searching for all users with query: {}", queryString);
        return searchAll(evaluateQuery(queryString));
    }

    @Override
    public Page<IUser> searchAll(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorIsMultiple(evaluator);
        checkEvaluatorResourceType(evaluator);
        updateWithDefaultPageableIfMissing(evaluator, log);

        log.debug("Searching for all users using MongoDB with pagination: page={}, size={}",
                evaluator.getPageable().getPageNumber(), evaluator.getPageable().getPageSize());
        log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
        Page<IUser> result = userService.search(evaluator.getFullMongoQuery(), evaluator.getPageable());
        log.trace("MongoDB search all result: page size={}, total elements={}", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    @Override
    public long count(String queryString) {
        log.debug("Counting users with query: {}", queryString);
        return count(evaluateQuery(queryString));
    }

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

    @Override
    public boolean exists(String queryString) {
        log.debug("Checking existence of user with query: {}", queryString);
        return exists(evaluateQuery(queryString));
    }

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
