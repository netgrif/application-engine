package com.netgrif.application.engine.pfql.service.taskresource;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.IResourceSearchService;
import com.netgrif.application.engine.pfql.service.QueryLangEvaluator;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.evaluateQuery;

/**
 * Service implementation for searching Task resources using query language expressions.
 * <p>
 * Provides functionality to search for tasks using both MongoDB and Elasticsearch backends.
 * The service automatically determines which search backend to use based on the query
 * evaluator configuration. It supports single result retrieval, paginated searches,
 * counting matches, and existence checks.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSearchService implements IResourceSearchService<Task> {

    private final ITaskService taskService;
    private final IElasticTaskService elasticTaskService;
    private final IUserService userService;

    /**
     * Returns the query type handled by this service.
     *
     * @return {@link QueryType#TASK} indicating this service handles task queries
     */
    @Override
    public QueryType getQueryResourceType() {
        return QueryType.TASK;
    }

    /**
     * Searches for a single task matching the provided query string.
     *
     * @param queryString the query string to be evaluated and executed
     * @return the matching task, or null if no task is found
     */
    @Override
    public Task searchOne(String queryString) {
        log.debug("Searching for single task with query: {}", queryString);
        return searchOne(evaluateQuery(queryString));
    }

    /**
     * Searches for a single task using a pre-evaluated query expression.
     * <p>
     * The search is executed using either Elasticsearch or MongoDB based on the
     * evaluator configuration. This method validates that the query expects a single
     * result and throws an exception if multiple results are expected.
     * </p>
     *
     * @param evaluator the pre-evaluated query expression containing search criteria
     * @return the matching task, or null if no task is found
     * @throws IllegalArgumentException if evaluator is null or if the query expects multiple results
     */
    @Override
    public Task searchOne(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorIsSingle(evaluator);
        checkEvaluatorResourceType(evaluator);

        log.debug("Searching for single task using {}", evaluator.getSearchWithElastic() ? "Elasticsearch" : "MongoDB");
        if (evaluator.getSearchWithElastic()) {
            log.trace("Executing Elasticsearch query: {}", evaluator.getFullElasticQuery());
            Page<Task> taskInPage = findTasksElastic(evaluator.getFullElasticQuery(), PageRequest.of(0, 1));
            Task result = taskInPage.getContent().stream().findFirst().orElse(null);
            log.trace("Elasticsearch search one result: {}", result != null ? result.getStringId() : "null");
            return result;
        } else {
            log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
            Task result = taskService.searchOne(evaluator.getFullMongoQuery());
            log.trace("MongoDB search one result: {}", result != null ? result.getStringId() : "null");
            return result;
        }
    }

    /**
     * Searches for all tasks matching the provided query string.
     *
     * @param queryString the query string to be evaluated and executed
     * @return a page of matching tasks
     */
    @Override
    public Page<Task> searchAll(String queryString) {
        log.debug("Searching for all tasks with query: {}", queryString);
        return searchAll(evaluateQuery(queryString));
    }

    /**
     * Searches for all tasks using a pre-evaluated query expression.
     * <p>
     * The search is executed using either Elasticsearch or MongoDB based on the
     * evaluator configuration. This method validates that the query expects multiple
     * results and throws an exception if a single result is expected.
     * </p>
     *
     * @param evaluator the pre-evaluated query expression containing search criteria and pagination info
     * @return a page of matching tasks
     * @throws IllegalArgumentException if evaluator is null or if the query expects a single result
     */
    @Override
    public Page<Task> searchAll(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorIsMultiple(evaluator);
        checkEvaluatorResourceType(evaluator);
        updateWithDefaultPageableIfMissing(evaluator, log);

        log.debug("Searching for all tasks using {}", evaluator.getSearchWithElastic() ? "Elasticsearch" : "MongoDB");
        if (evaluator.getSearchWithElastic()) {
            log.trace("Executing Elasticsearch query: {}", evaluator.getFullElasticQuery());
            Page<Task> result = findTasksElastic(evaluator.getFullElasticQuery(), evaluator.getPageable());
            log.trace("Elasticsearch search all result: page size={}, total elements={}", result.getNumberOfElements(), result.getTotalElements());
            return result;
        } else {
            log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
            Page<Task> result = taskService.search(evaluator.getFullMongoQuery(), evaluator.getPageable());
            log.trace("MongoDB search all result: page size={}, total elements={}", result.getNumberOfElements(), result.getTotalElements());
            return result;
        }
    }

    /**
     * Counts the number of tasks matching the provided query string.
     *
     * @param queryString the query string to be evaluated and executed
     * @return the count of matching tasks
     */
    @Override
    public long count(String queryString) {
        log.debug("Counting tasks with query: {}", queryString);
        return count(evaluateQuery(queryString));
    }

    /**
     * Counts the number of tasks using a pre-evaluated query expression.
     * <p>
     * The count is executed using either Elasticsearch or MongoDB based on the
     * evaluator configuration.
     * </p>
     *
     * @param evaluator the pre-evaluated query expression containing search criteria
     * @return the count of matching tasks
     * @throws IllegalArgumentException if evaluator is null
     */
    @Override
    public long count(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorResourceType(evaluator);

        log.debug("Counting tasks using {}", evaluator.getSearchWithElastic() ? "Elasticsearch" : "MongoDB");
        if (evaluator.getSearchWithElastic()) {
            log.trace("Executing Elasticsearch count query: {}", evaluator.getFullElasticQuery());
            long result = countTasksElastic(evaluator.getFullElasticQuery());
            log.trace("Elasticsearch count result: {}", result);
            return result;
        } else {
            log.trace("Executing MongoDB count query: {}", evaluator.getFullMongoQuery());
            long result = taskService.count(evaluator.getFullMongoQuery());
            log.trace("MongoDB count result: {}", result);
            return result;
        }
    }

    /**
     * Checks whether any tasks exist that match the provided query string.
     *
     * @param queryString the query string to be evaluated and executed
     * @return true if at least one matching task exists, false otherwise
     */
    @Override
    public boolean exists(String queryString) {
        log.debug("Checking existence of task with query: {}", queryString);
        return exists(evaluateQuery(queryString));
    }

    /**
     * Checks whether any tasks exist using a pre-evaluated query expression.
     * <p>
     * The existence check is executed using either Elasticsearch or MongoDB based on the
     * evaluator configuration.
     * </p>
     *
     * @param evaluator the pre-evaluated query expression containing search criteria
     * @return true if at least one matching task exists, false otherwise
     * @throws IllegalArgumentException if evaluator is null
     */
    @Override
    public boolean exists(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorResourceType(evaluator);

        log.debug("Checking existence of tasks using {}", evaluator.getSearchWithElastic() ? "Elasticsearch" : "MongoDB");
        if (evaluator.getSearchWithElastic()) {
            log.trace("Executing Elasticsearch exists query: {}", evaluator.getFullElasticQuery());
            boolean result = existsTasksElastic(evaluator.getFullElasticQuery());
            log.trace("Elasticsearch exists result: {}", result);
            return result;
        } else {
            log.trace("Executing MongoDB exists query: {}", evaluator.getFullMongoQuery());
            boolean result = taskService.exists(evaluator.getFullMongoQuery());
            log.trace("MongoDB exists result: {}", result);
            return result;
        }
    }

    /**
     * Counts tasks using Elasticsearch.
     *
     * @param elasticQuery the Elasticsearch query string
     * @return the count of matching tasks
     */
    private long countTasksElastic(String elasticQuery) {
        ElasticTaskSearchRequest taskSearchRequest = new ElasticTaskSearchRequest();
        taskSearchRequest.query = elasticQuery;
        return elasticTaskService.count(List.of(taskSearchRequest), userService.getLoggedOrSystem().transformToLoggedUser(),
                LocaleContextHolder.getLocale(), false);
    }

    /**
     * Finds tasks using Elasticsearch with pagination support.
     *
     * @param elasticQuery the Elasticsearch query string
     * @param pageable     the pagination information
     * @return a page of matching tasks
     */
    private Page<Task> findTasksElastic(String elasticQuery, Pageable pageable) {
        ElasticTaskSearchRequest taskSearchRequest = new ElasticTaskSearchRequest();
        taskSearchRequest.query = elasticQuery;
        return elasticTaskService.search(List.of(taskSearchRequest), userService.getLoggedOrSystem().transformToLoggedUser(),
                pageable, LocaleContextHolder.getLocale(), false);
    }

    /**
     * Checks whether any tasks exist using Elasticsearch.
     *
     * @param elasticQuery the Elasticsearch query string
     * @return true if at least one matching task exists, false otherwise
     */
    private boolean existsTasksElastic(String elasticQuery) {
        return countTasksElastic(elasticQuery) > 0;
    }
    
}
