package com.netgrif.application.engine.pfql.service.processresource;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.IResourceSearchService;
import com.netgrif.application.engine.pfql.service.QueryLangEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.evaluateQuery;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessSearchService implements IResourceSearchService<PetriNet> {

    private final IPetriNetService petriNetService;

    /**
     * Returns the query type handled by this service.
     *
     * @return {@link QueryType#PROCESS} indicating this service handles process queries
     */
    @Override
    public QueryType getQueryResourceType() {
        return QueryType.PROCESS;
    }

    /**
     * Searches for a single process that matches the provided query string.
     * <p>
     * This method parses the query string into an evaluator and delegates to
     * {@link #searchOne(QueryLangEvaluator)} for execution.
     * </p>
     *
     * @param queryString the query string to be evaluated and executed
     * @return the matching {@link PetriNet} process, or null if no match is found
     * @throws IllegalArgumentException if the query string results in a multiple-results query
     */
    @Override
    public PetriNet searchOne(String queryString) {
        log.debug("Searching for single process with query: {}", queryString);
        return searchOne(evaluateQuery(queryString));
    }

    /**
     * Searches for a single process using a pre-evaluated query expression.
     * <p>
     * This method validates that the evaluator is configured for single-result queries
     * and currently executes the search using MongoDB. Future implementations will
     * support Elasticsearch as an alternative search backend.
     * </p>
     *
     * @param evaluator the query evaluator containing the parsed query and search configuration
     * @return the matching {@link PetriNet} process, or null if no match is found
     * @throws IllegalArgumentException if evaluator is null or configured for multiple results
     */
    @Override
    public PetriNet searchOne(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorMultiplicity(evaluator);
        checkEvaluatorResourceType(evaluator);
        
        // todo implement Elasticsearch search (service layer and evaluator layer)
        
        log.debug("Searching for single process using MongoDB");
        log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
        Page<PetriNet> processAsPage = petriNetService.search(evaluator.getFullMongoQuery(), PageRequest.of(0, 1));
        Optional<PetriNet> processOpt = processAsPage.getContent().stream().findFirst();
        log.trace("MongoDB search one result: {}", processOpt.isPresent() ? processOpt.get().getStringId() : "null");
        return processOpt.orElse(null);
    }

    /**
     * Searches for all processes that match the provided query string.
     * <p>
     * This method parses the query string into an evaluator and delegates to
     * {@link #searchAll(QueryLangEvaluator)} for execution.
     * </p>
     *
     * @param queryString the query string to be evaluated and executed
     * @return a page of matching {@link PetriNet} processes
     * @throws IllegalArgumentException if the query string results in a single-result query
     */
    @Override
    public Page<PetriNet> searchAll(String queryString) {
        log.debug("Searching for all processes with query: {}", queryString);
        return searchAll(evaluateQuery(queryString));
    }

    /**
     * Searches for all processes using a pre-evaluated query expression.
     * <p>
     * This method validates that the evaluator is configured for multiple-result queries
     * and executes the search with pagination support. Currently uses MongoDB as the
     * search backend. Future implementations will support Elasticsearch.
     * </p>
     *
     * @param evaluator the query evaluator containing the parsed query, pagination, and search configuration
     * @return a page of matching {@link PetriNet} processes
     * @throws IllegalArgumentException if evaluator is null or configured for single result
     */
    @Override
    public Page<PetriNet> searchAll(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorMultiplicity(evaluator);
        checkEvaluatorResourceType(evaluator);
        
        // todo implement Elasticsearch search (service layer and evaluator layer)

        log.debug("Searching for all processes using MongoDB");
        log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
        Page<PetriNet> result = petriNetService.search(evaluator.getFullMongoQuery(), evaluator.getPageable());
        log.trace("MongoDB search all result: page size={}, total elements={}", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }

    /**
     * Counts the number of processes that match the provided query string.
     * <p>
     * This method parses the query string into an evaluator and delegates to
     * {@link #count(QueryLangEvaluator)} for execution.
     * </p>
     *
     * @param queryString the query string to be evaluated and executed
     * @return the count of matching processes
     */
    @Override
    public long count(String queryString) {
        log.debug("Counting processes with query: {}", queryString);
        return count(evaluateQuery(queryString));
    }

    /**
     * Counts the number of processes using a pre-evaluated query expression.
     * <p>
     * This method executes a count operation without retrieving the actual process data.
     * Currently uses MongoDB as the search backend. Future implementations will support
     * Elasticsearch.
     * </p>
     *
     * @param evaluator the query evaluator containing the parsed query and search configuration
     * @return the count of processes matching the query
     * @throws IllegalArgumentException if evaluator is null
     */
    @Override
    public long count(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorResourceType(evaluator);

        // todo implement Elasticsearch search (service layer and evaluator layer)
        
        log.debug("Counting processes using MongoDB");
        log.trace("Executing MongoDB count query: {}", evaluator.getFullMongoQuery());
        long result = petriNetService.count(evaluator.getFullMongoQuery());
        log.trace("MongoDB count result: {}", result);
        return result;
    }

    /**
     * Checks whether any processes exist that match the provided query string.
     * <p>
     * This method parses the query string into an evaluator and delegates to
     * {@link #exists(QueryLangEvaluator)} for execution.
     * </p>
     *
     * @param queryString the query string to be evaluated and executed
     * @return true if at least one matching process exists, false otherwise
     */
    @Override
    public boolean exists(String queryString) {
        log.debug("Checking existence of process with query: {}", queryString);
        return exists(evaluateQuery(queryString));
    }

    /**
     * Checks whether any processes exist using a pre-evaluated query expression.
     * <p>
     * This method performs an existence check without retrieving or counting the actual
     * process data, making it more efficient than count or search operations when only
     * existence needs to be verified. Currently uses MongoDB as the search backend.
     * Future implementations will support Elasticsearch.
     * </p>
     *
     * @param evaluator the query evaluator containing the parsed query and search configuration
     * @return true if at least one matching process exists, false otherwise
     * @throws IllegalArgumentException if evaluator is null
     */
    @Override
    public boolean exists(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorResourceType(evaluator);

        // todo implement Elasticsearch search (service layer and evaluator layer)
        
        log.debug("Checking existence of processes using MongoDB");
        log.trace("Executing MongoDB exists query: {}", evaluator.getFullMongoQuery());
        boolean result = petriNetService.exists(evaluator.getFullMongoQuery());
        log.trace("MongoDB exists result: {}", result);
        return result;
    }
}
