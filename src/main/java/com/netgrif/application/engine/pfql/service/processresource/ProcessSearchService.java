package com.netgrif.application.engine.pfql.service.processresource;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.pfql.domain.antlr4.QueryLangParser;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.AbstractResourceSearchService;
import com.netgrif.application.engine.pfql.service.QueryLangEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.buildResourcePrefix;
import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.hasResourcePrefix;


// todo 2483 doc
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessSearchService extends AbstractResourceSearchService<PetriNet> {
    protected static List<Integer> allowedResourcePrefixes = List.of(QueryLangParser.PROCESS, QueryLangParser.PROCESSES);

    protected final IPetriNetService petriNetService;

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
     // todo 2483
     * @param query the query string after placeholder substitution
     * @return
     */
    @Override
    protected String ensurePrefix(String query, boolean isMulti) {
        if (query == null || hasResourcePrefix(query, allowedResourcePrefixes)) {
            return query;
        }
        return buildResourcePrefix(isMulti ? QueryLangParser.PROCESSES : QueryLangParser.PROCESS) + query;
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
    protected PetriNet doSearchOne(QueryLangEvaluator evaluator) {
        // todo implement Elasticsearch search (service layer and evaluator layer)
        
        log.debug("Searching for single process using MongoDB");
        log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
        PetriNet result = petriNetService.searchOne(evaluator.getFullMongoQuery());
        log.trace("MongoDB search one result: {}", result != null ? result.getStringId() : "null");
        return result;
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
    protected Page<PetriNet> doSearchAll(QueryLangEvaluator evaluator) {
        // todo implement Elasticsearch search (service layer and evaluator layer)

        log.debug("Searching for all processes using MongoDB");
        log.trace("Executing MongoDB query: {}", evaluator.getFullMongoQuery());
        Page<PetriNet> result = petriNetService.search(evaluator.getFullMongoQuery(), evaluator.getPageable());
        log.trace("MongoDB search all result: page size={}, total elements={}", result.getNumberOfElements(), result.getTotalElements());
        return result;
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
    protected long doCount(QueryLangEvaluator evaluator) {
        // todo implement Elasticsearch search (service layer and evaluator layer)
        
        log.debug("Counting processes using MongoDB");
        log.trace("Executing MongoDB count query: {}", evaluator.getFullMongoQuery());
        long result = petriNetService.count(evaluator.getFullMongoQuery());
        log.trace("MongoDB count result: {}", result);
        return result;
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
    protected boolean doExists(QueryLangEvaluator evaluator) {
        // todo implement Elasticsearch search (service layer and evaluator layer)
        
        log.debug("Checking existence of processes using MongoDB");
        log.trace("Executing MongoDB exists query: {}", evaluator.getFullMongoQuery());
        boolean result = petriNetService.exists(evaluator.getFullMongoQuery());
        log.trace("MongoDB exists result: {}", result);
        return result;
    }
}
