package com.netgrif.application.engine.pfql.service.taskresource;

import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.IResourceSearchService;
import com.netgrif.application.engine.pfql.service.QueryLangEvaluator;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.evaluateQuery;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSearchService implements IResourceSearchService<Task> {
    // todo 2443 javadoc
    // todo 2443 logging

    @Override
    public QueryType getQueryType() {
        return QueryType.TASK;
    }

    @Override
    public Task searchOne(String queryString) {
        log.debug("Searching for single task with query: {}", queryString);
        return searchOne(evaluateQuery(queryString));
    }

    @Override
    public Task searchOne(QueryLangEvaluator evaluator) {
        return null;
    }

    @Override
    public Page<Task> searchAll(String queryString) {
        log.debug("Searching for all tasks with query: {}", queryString);
        return searchAll(evaluateQuery(queryString));
    }

    @Override
    public Page<Task> searchAll(QueryLangEvaluator evaluator) {
        return null;
    }

    @Override
    public long count(String queryString) {
        log.debug("Counting tasks with query: {}", queryString);
        return count(evaluateQuery(queryString));
    }

    @Override
    public long count(QueryLangEvaluator evaluator) {
        return 0;
    }

    @Override
    public boolean exists(String queryString) {
        log.debug("Checking existence of task with query: {}", queryString);
        return exists(evaluateQuery(queryString));
    }

    @Override
    public boolean exists(QueryLangEvaluator evaluator) {
        return false;
    }
}
