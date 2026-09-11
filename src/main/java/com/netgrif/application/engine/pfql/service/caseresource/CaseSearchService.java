package com.netgrif.application.engine.pfql.service.caseresource;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.pfql.domain.antlr4.QueryLangParser;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.AbstractResourceSearchService;
import com.netgrif.application.engine.pfql.service.QueryLangEvaluator;
import com.netgrif.application.engine.pfql.service.formatters.QueryLangPlaceholderHandler;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.buildResourcePrefix;
import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.hasResourcePrefix;

/**
 * Service implementation for searching and querying Case resources.
 * Supports both MongoDB and Elasticsearch-based searches depending on the query configuration.
 * Provides methods to search for single or multiple cases, count matching cases, and check existence.
 */
@Slf4j
@Service
public class CaseSearchService extends AbstractResourceSearchService<Case> {

    protected static List<Integer> allowedResourcePrefixes = List.of(QueryLangParser.CASE, QueryLangParser.CASES);

    protected final IWorkflowService workflowService;
    protected final IElasticCaseService elasticCaseService;
    protected final IUserService userService;

    public CaseSearchService(QueryLangPlaceholderHandler placeholderHandler, IWorkflowService workflowService,
                             IElasticCaseService elasticCaseService, IUserService userService) {
        super(placeholderHandler);
        this.workflowService = workflowService;
        this.elasticCaseService = elasticCaseService;
        this.userService = userService;
    }

    /**
     * Returns the query type handled by this service.
     *
     * @return the QueryType.CASE indicating this service handles case queries
     */
    @Override
    public QueryType getQueryResourceType() {
        return QueryType.CASE;
    }

    @Override
    protected String ensurePrefix(String query, boolean isMulti) {
        if (query == null || hasResourcePrefix(query, allowedResourcePrefixes)) {
            return query;
        }
        return buildResourcePrefix(isMulti ? QueryLangParser.CASES : QueryLangParser.CASE) + query;
    }

    /**
     * Searches for a single case using a pre-evaluated query evaluator.
     * Routes the search to either Elasticsearch or MongoDB based on the evaluator configuration.
     *
     * @param evaluator the query evaluator containing the parsed query and configuration
     * @return the first matching Case, or null if no match is found
     * @throws IllegalArgumentException if the evaluator is null or configured for multiple results
     */
    @Override
    protected Case doSearchOne(QueryLangEvaluator evaluator) {
        log.debug("Searching for single case using {}", evaluator.getSearchWithElastic() ? "Elasticsearch" : "MongoDB");
        if (evaluator.getSearchWithElastic()) {
            log.trace("Executing Elasticsearch query: {}", evaluator.getFullElasticQuery());
            Page<Case> caseInPage = findCasesElastic(evaluator.getFullElasticQuery(), PageRequest.of(0, 1));
            Case result = caseInPage.getContent().stream().findFirst().orElse(null);
            log.trace("Elasticsearch search one result: {}", result != null ? result.getStringId() : "null");
            return result;
        } else {
            log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
            Case result = workflowService.searchOne(evaluator.getFullMongoQuery());
            log.trace("MongoDB search one result: {}", result != null ? result.getStringId() : "null");
            return result;
        }
    }

    /**
     * Searches for all cases using a pre-evaluated query evaluator.
     * Routes the search to either Elasticsearch or MongoDB based on the evaluator configuration.
     * Supports pagination through the evaluator's pageable configuration.
     *
     * @param evaluator the query evaluator containing the parsed query, pagination, and configuration
     * @return a Page containing all matching Cases
     * @throws IllegalArgumentException if the evaluator is null or configured for single result
     */
    @Override
    protected Page<Case> doSearchAll(QueryLangEvaluator evaluator) {
        log.debug("Searching for all cases using {} with pagination: page={}, size={}",
                evaluator.getSearchWithElastic() ? "Elasticsearch" : "MongoDB",
                evaluator.getPageable().getPageNumber(), evaluator.getPageable().getPageSize());
        if (evaluator.getSearchWithElastic()) {
            log.trace("Executing Elasticsearch query: {}", evaluator.getFullElasticQuery());
            Page<Case> result = findCasesElastic(evaluator.getFullElasticQuery(), evaluator.getPageable());
            log.trace("Elasticsearch search all result: page size={}, total elements={}", result.getNumberOfElements(), result.getTotalElements());
            return result;
        } else {
            log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
            Page<Case> result = workflowService.search(evaluator.getFullMongoQuery(), evaluator.getPageable());
            log.trace("MongoDB search all result: page size={}, total elements={}", result.getNumberOfElements(), result.getTotalElements());
            return result;
        }
    }

    /**
     * Counts the number of cases using a pre-evaluated query evaluator.
     * Routes the count operation to either Elasticsearch or MongoDB based on the evaluator configuration.
     *
     * @param evaluator the query evaluator containing the parsed query and configuration
     * @return the count of matching cases
     * @throws IllegalArgumentException if the evaluator is null
     */
    @Override
    protected long doCount(QueryLangEvaluator evaluator) {
        log.debug("Counting cases using {}", evaluator.getSearchWithElastic() ? "Elasticsearch" : "MongoDB");
        if (evaluator.getSearchWithElastic()) {
            log.trace("Executing Elasticsearch count query: {}", evaluator.getFullElasticQuery());
            long result = countCasesElastic(evaluator.getFullElasticQuery());
            log.trace("Elasticsearch count result: {}", result);
            return result;
        } else {
            log.trace("Executing MongoDB count query: {}", evaluator.getFullMongoQuery());
            long result = workflowService.count(evaluator.getFullMongoQuery());
            log.trace("MongoDB count result: {}", result);
            return result;
        }
    }

    /**
     * Checks whether any cases exist using a pre-evaluated query evaluator.
     * Routes the existence check to either Elasticsearch or MongoDB based on the evaluator configuration.
     *
     * @param evaluator the query evaluator containing the parsed query and configuration
     * @return true if at least one matching case exists, false otherwise
     * @throws IllegalArgumentException if the evaluator is null
     */
    @Override
    protected boolean doExists(QueryLangEvaluator evaluator) {
        log.debug("Checking existence of cases using {}", evaluator.getSearchWithElastic() ? "Elasticsearch" : "MongoDB");
        if (evaluator.getSearchWithElastic()) {
            log.trace("Executing Elasticsearch exists query: {}", evaluator.getFullElasticQuery());
            boolean result = existsCasesElastic(evaluator.getFullElasticQuery());
            log.trace("Elasticsearch exists result: {}", result);
            return result;
        } else {
            log.trace("Executing MongoDB exists query: {}", evaluator.getFullMongoQuery());
            boolean result = workflowService.exists(evaluator.getFullMongoQuery());
            log.trace("MongoDB exists result: {}", result);
            return result;
        }
    }

    protected long countCasesElastic(String elasticQuery) {
        CaseSearchRequest caseSearchRequest = new CaseSearchRequest();
        caseSearchRequest.query = elasticQuery;
        return elasticCaseService.count(List.of(caseSearchRequest), userService.getLoggedOrSystem().transformToLoggedUser(),
                LocaleContextHolder.getLocale(), false);
    }

    protected Page<Case> findCasesElastic(String elasticQuery, Pageable pageable) {
        CaseSearchRequest caseSearchRequest = new CaseSearchRequest();
        caseSearchRequest.query = elasticQuery;
        return elasticCaseService.search(List.of(caseSearchRequest), userService.getLoggedOrSystem().transformToLoggedUser(),
                pageable, LocaleContextHolder.getLocale(), false);
    }

    protected boolean existsCasesElastic(String elasticQuery) {
        return countCasesElastic(elasticQuery) > 0;
    }
}
