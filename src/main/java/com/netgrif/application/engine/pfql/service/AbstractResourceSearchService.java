package com.netgrif.application.engine.pfql.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.evaluateQuery;
import static com.netgrif.application.engine.pfql.service.utils.SearchUtils.formatPlaceholders;

/**
 * Abstract base class for resource search services providing shared query pre-processing.
 * <p>
 * Handles common pre-processing steps:
 * <ol>
 *   <li>Formatter bracket substitution – fills {@code {}} placeholders with provided arguments</li>
 *   <li>PFQL prefix check/injection – implemented individually by each subclass</li>
 * </ol>
 */
@Slf4j
public abstract class AbstractResourceSearchService<Resource> implements IResourceSearchService<Resource> {

    /**
     * Pre-processes the raw query string: fills {@code {}} placeholders and ensures a correct PFQL prefix.
     *
     * @param rawQuery the raw query string, possibly with {@code {}} placeholders
     * @param isMulti todo 2483
     * @param args     arguments to substitute into {@code {}} placeholders (in order)
     * @return the fully pre-processed query string ready for evaluation
     */
    protected String preProcess(String rawQuery, boolean isMulti, Object... args) {
        String formatted = formatPlaceholders(rawQuery, args);
        return ensurePrefix(formatted, isMulti);
    }

    /**
     * Ensures the query string has the correct PFQL resource prefix.
     * Each implementation defines which prefix is expected and how to inject it if missing.
     *
     * @param query the query string after placeholder substitution
     * @param isMulti todo 2483
     * @return the query string with the correct prefix guaranteed
     */
    protected abstract String ensurePrefix(String query, boolean isMulti);

    protected abstract Resource doSearchOne(QueryLangEvaluator evaluator);

    protected abstract Page<Resource> doSearchAll(QueryLangEvaluator evaluator);

    protected abstract long doCount(QueryLangEvaluator evaluator);

    protected abstract boolean doExists(QueryLangEvaluator evaluator);

    // todo 2483 doc
    @Override
    public Resource searchOne(String queryString, Object... args) {
        final String processedQuery = preProcess(queryString, false, args);
        log.debug("Searching one with query: {}", processedQuery);
        return searchOne(evaluateQuery(processedQuery));
    }

    @Override
    public Resource searchOne(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorIsSingle(evaluator);
        checkEvaluatorResourceType(evaluator);
        return doSearchOne(evaluator);
    }

    @Override
    public Page<Resource> searchAll(String queryString, Object... args) {
        final String processedQuery = preProcess(queryString, true, args);
        log.debug("Searching all with query: {}", processedQuery);
        return searchAll(evaluateQuery(processedQuery));
    }

    @Override
    public Page<Resource> searchAll(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorIsMultiple(evaluator);
        checkEvaluatorResourceType(evaluator);
        return doSearchAll(evaluator);
    }

    @Override
    public long count(String queryString, Object... args) {
        final String processedQuery = preProcess(queryString, true, args);
        log.debug("Counting with query: {}", processedQuery);
        return count(evaluateQuery(processedQuery));
    }

    @Override
    public long count(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorResourceType(evaluator);
        return doCount(evaluator);
    }

    @Override
    public boolean exists(String queryString, Object... args) {
        final String processedQuery = preProcess(queryString, false, args);
        log.debug("Checking existence with query: {}", processedQuery);
        return exists(evaluateQuery(processedQuery));
    }

    @Override
    public boolean exists(QueryLangEvaluator evaluator) {
        checkEvaluatorNotNull(evaluator);
        checkEvaluatorResourceType(evaluator);
        return doExists(evaluator);
    }
}
