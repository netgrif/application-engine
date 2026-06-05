package com.netgrif.application.engine.pfql.service;

import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.utils.SearchUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.evaluateQuery;

@Slf4j
@Service
public class SearchService implements ISearchService {

    private final Map<QueryType, IResourceSearchService<?>> serviceRegistry;

    public SearchService(List<IResourceSearchService<?>> services) {
        this.serviceRegistry = services.stream()
                .collect(Collectors.toMap(IResourceSearchService::getQueryResourceType, Function.identity()));

    }

    /**
     * Explains the provided query by parsing and describing its structure.
     *
     * @param input the query string to be explained
     * @return a human-readable explanation of the query structure
     */
    @Override
    public String explainQuery(String input) {
        log.debug("Explaining query: {}", input);
        String explanation = SearchUtils.explainQuery(input);
        log.trace("Query explanation result: {}", explanation);
        return explanation;
    }

    /**
     * Executes a search operation based on the provided query string.
     * Evaluates the query and delegates to the appropriate resource search service.
     * Returns either a single result or multiple results based on the query specification.
     *
     * @param input the query string to be executed
     * @return a single resource object or a page of resources depending on the query type
     */
    @Override
    public Object search(String input) {
        log.debug("Executing search with query: {}", input);
        QueryLangEvaluator evaluator = evaluateQuery(input);
        log.trace("Evaluated query type: {}, multiple: {}", evaluator.getResourceType(), evaluator.getMultiple());
        IResourceSearchService<?> service = this.serviceRegistry.get(evaluator.getResourceType());
        Object result = evaluator.getMultiple() ? service.searchAll(evaluator) : service.searchOne(evaluator);
        log.debug("Search completed, returning {} result", evaluator.getMultiple() ? "multiple" : "single");
        return result;
    }

    /**
     * Counts the number of resources that match the provided query string.
     *
     * @param input the query string to be evaluated
     * @return the count of matching resources
     */
    @Override
    public long count(String input) {
        log.debug("Counting resources with query: {}", input);
        QueryLangEvaluator evaluator = evaluateQuery(input);
        log.trace("Evaluated query type for count: {}", evaluator.getResourceType());
        IResourceSearchService<?> service = this.serviceRegistry.get(evaluator.getResourceType());
        long count = service.count(evaluator);
        log.debug("Count completed, result: {}", count);
        return count;
    }

    /**
     * Checks whether any resources exist that match the provided query string.
     *
     * @param input the query string to be evaluated
     * @return true if at least one matching resource exists, false otherwise
     */
    @Override
    public boolean exists(String input) {
        log.debug("Checking existence with query: {}", input);
        QueryLangEvaluator evaluator = evaluateQuery(input);
        log.trace("Evaluated query type for exists: {}", evaluator.getResourceType());
        IResourceSearchService<?> service = this.serviceRegistry.get(evaluator.getResourceType());
        boolean exists = service.exists(evaluator);
        log.debug("Existence check completed, result: {}", exists);
        return exists;
    }
}
