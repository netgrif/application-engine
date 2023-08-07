package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.domain.ElasticQueryConstants;
import com.netgrif.application.engine.elastic.domain.IndexAwareElasticSearchRequest;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCasePrioritySearch;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.startup.SystemUserRunner;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.bson.types.ObjectId;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class ElasticCaseService extends ElasticViewPermissionService implements IElasticCaseService {

    private static final Logger log = LoggerFactory.getLogger(ElasticCaseService.class);

    private IWorkflowService workflowService;

    private Executor executors;

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private IElasticIndexService indexService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private IElasticCasePrioritySearch iElasticCasePrioritySearch;

    @Autowired
    private SystemUserRunner systemUserRunner;

//    @Autowired
//    private IImpersonationElasticFilterService impersonationElasticFilterService;

    @Autowired
    public ElasticCaseService(ElasticsearchRestTemplate template, Executor executors) {
        this.template = template;
        this.executors = executors;
    }

    @Autowired
    @Lazy
    public void setWorkflowService(IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void remove(String caseId) {
        log.warn("calling remove(String caseId): Use remove(String caseId, String uriNodeId) instead");
        executors.execute(caseId, () -> {
            template.delete(getQueryForProperty("stringId", caseId),
                    ElasticCase.class,
                    IndexCoordinates.of(indexService.getAllDynamicIndexes().toArray(new String[0])));
            log.info("[" + caseId + "]: Case \"" + caseId + "\" deleted");
        });
    }

    @Override
    public void remove(String caseId, String uriNodeId) {
        executors.execute(caseId, () -> {
            template.delete(caseId, IndexCoordinates.of(getIndex(uriNodeId)));
            log.info("[" + caseId + "][" + uriNodeId + "]: Case \"" + caseId + "\" deleted");
        });
    }

    @Override
    public void removeByPetriNetId(String processId) {
        executors.execute(processId, () -> {
            PetriNet net = petriNetService.get(new ObjectId(processId));
            template.delete(getQueryForProperty("processId", processId), ElasticCase.class, IndexCoordinates.of(getIndex(net.getUriNodeId())));
            log.info("[" + processId + "][" + net.getUriNodeId() + "]: All cases of Petri Net with id \"" + processId + "\" deleted");
        });
    }

    @Override
    public void removeByPetriNetId(String processId, String uriNodeId) {
        executors.execute(processId, () -> {
            template.delete(getQueryForProperty("processId", processId), ElasticCase.class, IndexCoordinates.of(getIndex(uriNodeId)));
            log.info("[" + processId + "][" + uriNodeId + "]: All cases of Petri Net with id \"" + processId + "\" deleted");
        });
    }

    @Override
    public void indexNow(ElasticCase useCase) {
        index(useCase);
    }

    @Override
    public void index(ElasticCase useCase) {
        executors.execute(useCase.getStringId(), () -> {
            // stringId might not be indexed fast enough to prevent duplicity,
            // we need to be able to search based on a case property that is indexed immediately: id
//            useCase.setId(useCase.getStringId());
            String index = getIndex(useCase.getUriNodeId());
            IndexCoordinates allIndexes = IndexCoordinates.of(getAllIndexes().toArray(new String[0]));
            log.debug("[" + useCase.getStringId() + "] Indexing case in " + index);

            List<ElasticCase> existing = findAllByStringIdOrId(useCase.getStringId(), useCase.getStringId(), allIndexes);
            if (existing.size() == 1) {
                ElasticCase oneExisting = existing.get(0);
                String oneExistingIndex = getIndex(oneExisting.getUriNodeId());
                oneExisting.update(useCase);
                if (!oneExistingIndex.equals(index)) {
                    template.delete(oneExisting.getId(), IndexCoordinates.of(oneExistingIndex));
                }
            } else if (existing.size() > 1) {
                // delete by id does not support multiple indexes in IndexCoordinates
                existing.forEach(esCase -> template.delete(esCase.getId(), IndexCoordinates.of(getIndex(esCase.getUriNodeId()))));
            }
            doIndex(useCase, index);
            log.debug("[" + useCase.getStringId() + "] Indexed case in " + index);
        });
    }

    protected void doIndex(ElasticCase useCase, String index) {
        IndexQuery indexQuery = new IndexQueryBuilder()
//                .withId(useCase.getId()) //TODO: ON ?
                .withObject(useCase)
                .build();
        template.index(indexQuery, IndexCoordinates.of(index));
    }


    @Override
    public Page<Case> search(List<CaseSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        IndexCoordinates indexCoordinates = validateRequestAndExtractIndexCoords(requests);

        NativeSearchQuery query = buildQuery(requests, user, pageable, locale, isIntersection);
        List<Case> casePage;
        long total;
        if (query != null) {
            SearchHits<ElasticCase> hits = template.search(query, ElasticCase.class, indexCoordinates);
            Page<ElasticCase> indexedCases = (Page) SearchHitSupport.unwrapSearchHits(SearchHitSupport.searchPageFor(hits, query.getPageable()));
            casePage = workflowService.findAllById(indexedCases.get().map(ElasticCase::getStringId).collect(Collectors.toList()));
            total = indexedCases.getTotalElements();
        } else {
            casePage = Collections.emptyList();
            total = 0;
        }

        return new PageImpl<>(casePage, pageable, total);
    }

    @Override
    public List<ElasticCase> findAllByStringIdOrId(String stringId, String elasticId, IndexCoordinates indexCoordinates) {
        NativeSearchQuery query = getQueryForProperties(Map.ofEntries(
                entry("stringId", stringId),
                entry("_id", elasticId)
        ), 0, 100, false);
        SearchHits<ElasticCase> hits = template.search(query, ElasticCase.class, indexCoordinates);
        Page<ElasticCase> indexedCases = (Page) SearchHitSupport.unwrapSearchHits(SearchHitSupport.searchPageFor(hits, query.getPageable()));
        return indexedCases.getContent();
    }

    @Override
    public long countByLastModified(Case useCase, long timestamp) {
        IndexCoordinates index = IndexCoordinates.of(getIndex(useCase.getUriNodeId()));
        NativeSearchQuery query = getQueryForProperties(Map.ofEntries(
                entry("stringId", useCase.getStringId()),
                entry("lastModified", timestamp)
        ), 0, 100, true);
        return template.count(query, ElasticCase.class, index);
    }

    protected NativeSearchQuery getQueryForProperty(String property, String value) {
        return getQueryForProperty(property, value, 0, 100);
    }

    protected NativeSearchQuery getQueryForProperty(String property, String value, int page, int size) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        BoolQueryBuilder caseIdQuery = boolQuery();
        caseIdQuery.must(termQuery(property, value));
        return builder
                .withQuery(caseIdQuery)
                .withPageable(PageRequest.of(page, size))
                .build();
    }

    protected NativeSearchQuery getQueryForProperties(Map<String, Object> props, int page, int size, boolean intersection) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        List<BoolQueryBuilder> queries = props.entrySet().stream().map(entry -> {
            BoolQueryBuilder query = boolQuery();
            query.must(termQuery(entry.getKey(), entry.getValue()));
            return query;
        }).collect(Collectors.toList());
        BinaryOperator<BoolQueryBuilder> reductionOperator = intersection ? BoolQueryBuilder::must : BoolQueryBuilder::should;
        return builder
                .withQuery(queries.stream().reduce(new BoolQueryBuilder(), reductionOperator))
                .withPageable(PageRequest.of(page, size))
                .build();
    }

    @Override
    public long count(List<CaseSearchRequest> requests, LoggedUser user, Locale locale, Boolean isIntersection) {
        if (requests == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        IndexCoordinates indexCoordinates = validateRequestAndExtractIndexCoords(requests);

        NativeSearchQuery query = buildQuery(requests, user, new FullPageRequest(), locale, isIntersection);
        if (query != null) {
            return template.count(query, ElasticCase.class, indexCoordinates);
        } else {
            return 0;
        }
    }

    private NativeSearchQuery buildQuery(List<CaseSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        List<BoolQueryBuilder> singleQueries = requests.stream().map(request -> buildSingleQuery(request, user, locale)).collect(Collectors.toList());

        if (isIntersection && !singleQueries.stream().allMatch(Objects::nonNull)) {
            // one of the queries evaluates to empty set => the entire result is an empty set
            return null;
        } else if (!isIntersection) {
            singleQueries = singleQueries.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (singleQueries.size() == 0) {
                // all queries result in an empty set => the entire result is an empty set
                return null;
            }
        }

        BinaryOperator<BoolQueryBuilder> reductionOperator = isIntersection ? BoolQueryBuilder::must : BoolQueryBuilder::should;
        BoolQueryBuilder query = singleQueries.stream().reduce(new BoolQueryBuilder(), reductionOperator);

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        return builder
                .withQuery(query)
                .withPageable(pageable)
                .build();
    }

    private BoolQueryBuilder buildSingleQuery(CaseSearchRequest request, LoggedUser user, Locale locale) {
        BoolQueryBuilder query = boolQuery();
//        if (user.isImpersonating()) {
//            addImpersonationAllowedProcessesConstraint(query, user);
//        }
        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        if (!loggedOrImpersonated.getId().equals(systemUserRunner.getLoggedSystem().getId())) {
            buildViewPermissionQuery(query, loggedOrImpersonated);
        }
        buildPetriNetQuery(request, loggedOrImpersonated, query);
        buildAuthorQuery(request, query);
        buildTaskQuery(request, query);
        buildRoleQuery(request, query);
        buildDataQuery(request, query);
        buildFullTextQuery(request, query);
        buildStringQuery(request, query, loggedOrImpersonated);
        buildCaseIdQuery(request, query);
        buildUriNodeIdQuery(request, query);
        boolean resultAlwaysEmpty = buildGroupQuery(request, loggedOrImpersonated, locale, query);

        // TODO: filtered query https://stackoverflow.com/questions/28116404/filtered-query-using-nativesearchquerybuilder-in-spring-data-elasticsearch

        if (resultAlwaysEmpty)
            return null;
        else
            return query;
    }

//    private void addImpersonationAllowedProcessesConstraint(BoolQueryBuilder query, LoggedUser user) {
//        impersonationElasticFilterService.addImpersonationAllowedProcessesConstraint(query, user);
//    }

    private void buildPetriNetQuery(CaseSearchRequest request, LoggedUser user, BoolQueryBuilder query) {
        if (request.process == null || request.process.isEmpty()) {
            return;
        }

        BoolQueryBuilder petriNetQuery = boolQuery();

        for (CaseSearchRequest.PetriNet process : request.process) {
            if (process.identifier != null) {
                petriNetQuery.should(termQuery("processIdentifier", process.identifier));
            }
            if (process.processId != null) {
                petriNetQuery.should(termQuery("processId", process.processId));
            }
        }

        query.filter(petriNetQuery);
    }

    /**
     * <pre>
     * {
     *     "author": {
     *         "email": "user@customer.com"
     *     }
     * }
     * </pre><br>
     * <p>
     * Cases with author with (id 1 AND email "user@customer.com") OR (id 2)
     * <pre>
     * {
     *     "author": [{
     *         "id": 1
     *         "email": "user@customer.com"
     *     }, {
     *         "id": 2
     *     }
     *     ]
     * }
     * </pre><br>
     */
    private void buildAuthorQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.author == null || request.author.isEmpty()) {
            return;
        }

        BoolQueryBuilder authorsQuery = boolQuery();
        for (CaseSearchRequest.Author author : request.author) {
            BoolQueryBuilder authorQuery = boolQuery();
            if (author.email != null) {
                authorQuery.must(termQuery("authorEmail", author.email));
            }
            if (author.id != null) {
                authorQuery.must(matchQuery("author", author.id));
            }
            if (author.name != null) {
                authorQuery.must(termQuery("authorName", author.name));
            }
            authorsQuery.should(authorQuery);
        }

        query.filter(authorsQuery);
    }

    /**
     * Cases with tasks with import Id "nova_uloha"
     * <pre>
     * {
     *     "transition": "nova_uloha"
     * }
     * </pre>
     * <p>
     * Cases with tasks with import Id "nova_uloha" OR "kontrola"
     * <pre>
     * {
     *     "transition": [
     *         "nova_uloha",
     *         "kontrola"
     *     ]
     * }
     * </pre>
     */
    private void buildTaskQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.transition == null || request.transition.isEmpty()) {
            return;
        }

        BoolQueryBuilder taskQuery = boolQuery();
        for (String taskImportId : request.transition) {
            taskQuery.should(termQuery("taskIds", taskImportId));
        }

        query.filter(taskQuery);
    }

    /**
     * Cases with active role "5cb07b6ff05be15f0b972c36"
     * <pre>
     * {
     *     "role": "5cb07b6ff05be15f0b972c36"
     * }
     * </pre>
     * <p>
     * Cases with active role "5cb07b6ff05be15f0b972c36" OR "5cb07b6ff05be15f0b972c31"
     * <pre>
     * {
     *     "role" [
     *         "5cb07b6ff05be15f0b972c36",
     *         "5cb07b6ff05be15f0b972c31"
     *     ]
     * }
     * </pre>
     */
    private void buildRoleQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.role == null || request.role.isEmpty()) {
            return;
        }

        BoolQueryBuilder roleQuery = boolQuery();
        for (String roleId : request.role) {
            roleQuery.should(termQuery("enabledRoles", roleId));
        }

        query.filter(roleQuery);
    }

    /**
     * Cases where "text_field" has value EXACTLY "text" AND "number_field" has value EXACTLY "125".<br>
     * <pre>
     * {
     *     "data": {
     *         "text_field": "text",
     *         "number_field": "125"
     *     }
     * }
     * </pre>
     */
    private void buildDataQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.data == null || request.data.isEmpty()) {
            return;
        }

        BoolQueryBuilder dataQuery = boolQuery();
        for (Map.Entry<String, String> field : request.data.entrySet()) {
            if (field.getKey().contains("."))
                dataQuery.must(termQuery("dataSet." + field.getKey(), field.getValue()));
            else
                dataQuery.must(termQuery("dataSet." + field.getKey() + ".fulltextValue.keyword", field.getValue()));
        }

        query.filter(dataQuery);
    }

    private void buildFullTextQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.fullText == null || request.fullText.isEmpty()) {
            return;
        }

        // TODO: improvement? wildcard does not scale good
        QueryBuilder fullTextQuery = queryStringQuery("*" + request.fullText + "*").fields(iElasticCasePrioritySearch.fullTextFields());
        query.must(fullTextQuery);
    }

    /**
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html">Query String Query</a>
     */
    private void buildStringQuery(CaseSearchRequest request, BoolQueryBuilder query, LoggedUser user) {
        if (request.query == null || request.query.isEmpty()) {
            return;
        }

        String populatedQuery = request.query.replaceAll(ElasticQueryConstants.USER_ID_TEMPLATE, user.getId().toString());

        query.must(queryStringQuery(populatedQuery).allowLeadingWildcard(true).analyzeWildcard(true));
    }

    /**
     * Case with stringId "5cb07b6ff05be15f0b972c36"
     * <pre>
     * {
     *     "stringId": "5cb07b6ff05be15f0b972c36"
     * }
     * </pre>
     * <p>
     * Cases with stringId "5cb07b6ff05be15f0b972c36" OR "5cb07b6ff05be15f0b972c31"
     * <pre>
     * {
     *     "stringId" [
     *         "5cb07b6ff05be15f0b972c36",
     *         "5cb07b6ff05be15f0b972c31"
     *     ]
     * }
     * </pre>
     */
    private void buildCaseIdQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.stringId == null || request.stringId.isEmpty()) {
            return;
        }

        BoolQueryBuilder caseIdQuery = boolQuery();
        request.stringId.forEach(caseId -> caseIdQuery.should(termQuery("stringId", caseId)));
        query.filter(caseIdQuery);
    }

    private void buildUriNodeIdQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.uriNodeId == null || request.uriNodeId.isEmpty()) {
            return;
        }

        BoolQueryBuilder caseIdQuery = boolQuery();
        caseIdQuery.should(termQuery("uriNodeId", request.uriNodeId));
        query.filter(caseIdQuery);
    }

    /**
     * Cases that are instances of processes of group "5cb07b6ff05be15f0b972c36"
     * <pre>
     * {
     *     "group": "5cb07b6ff05be15f0b972c36"
     * }
     * </pre>
     * <p>
     * Cases that are instances of processes of group "5cb07b6ff05be15f0b972c36" OR "5cb07b6ff05be15f0b972c31"
     * <pre>
     * {
     *     "group" [
     *         "5cb07b6ff05be15f0b972c36",
     *         "5cb07b6ff05be15f0b972c31"
     *     ]
     * }
     * </pre>
     */
    private boolean buildGroupQuery(CaseSearchRequest request, LoggedUser user, Locale locale, BoolQueryBuilder query) {
        if (request.group == null || request.group.isEmpty()) {
            return false;
        }

        Map<String, Object> processQuery = new HashMap<>();
        processQuery.put("group", request.group);
        List<PetriNetReference> groupProcesses = this.petriNetService.search(processQuery, user, new FullPageRequest(), locale).getContent();
        if (groupProcesses.size() == 0)
            return true;

        BoolQueryBuilder groupQuery = boolQuery();
        groupProcesses.stream().map(PetriNetReference::getIdentifier)
                .map(netIdentifier -> termQuery("processIdentifier", netIdentifier))
                .forEach(groupQuery::should);
        query.filter(groupQuery);
        return false;
    }

    private Pageable resolveUnmappedSortAttributes(Pageable pageable) { //TODO: unuse ?? delete?
        List<Sort.Order> modifiedOrders = new ArrayList<>();
        pageable.getSort().iterator().forEachRemaining(order -> modifiedOrders.add(new Order(order.getDirection(), order.getProperty()).withUnmappedType("keyword")));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()).withSort(Sort.by(modifiedOrders));
    }

    protected String getIndex(String uriNodeId) {
        return indexService.getIndex(uriNodeId);
    }

    protected List<String> getAllIndexes() {
        return indexService.getAllIndexes();
    }

    protected List<String> getIndexes(IndexAwareElasticSearchRequest request) {
        List<String> result = new ArrayList<>();
        if (request.getIndexNames() != null && !request.getIndexNames().isEmpty()) {
            result.addAll(request.getIndexNames());
        }
        if (request.doQueryAll()) {
            result.addAll(indexService.getAllIndexes());
        }
        if (request.getMenuItemIds() != null) {
            result.addAll(request.getMenuItemIds().stream().map(indexService::getIndexByMenuItemId).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * validates request for nullability, collects requested indexes into IndexCoordinates
     * @param requests List<CaseSearchRequest> | IndexAwareSearchRequest
     * @return indexCoordinates
     */
    protected IndexCoordinates validateRequestAndExtractIndexCoords(List<CaseSearchRequest> requests) {
        if (requests == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }
        List<String> indexes;
        if (requests instanceof IndexAwareElasticSearchRequest) {
            indexes = getIndexes((IndexAwareElasticSearchRequest) requests);
        } else {
            indexes = getAllIndexes();
        }
        return IndexCoordinates.of(indexes.toArray(new String[0]));
    }


}