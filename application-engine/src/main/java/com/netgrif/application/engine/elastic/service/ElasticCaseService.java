package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.mapping.FieldType;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.domain.BulkOperationWrapper;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.domain.ElasticQueryConstants;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCasePrioritySearch;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.objects.event.events.workflow.IndexCaseEvent;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.elasticsearch.client.elc.Queries.matchQuery;
import static org.springframework.data.elasticsearch.client.elc.Queries.termQuery;

@Service
public class ElasticCaseService extends ElasticViewPermissionService implements IElasticCaseService {

    private static final Logger log = LoggerFactory.getLogger(ElasticCaseService.class);

    protected final ElasticCaseRepository repository;
    protected final ElasticsearchTemplate template;
    protected final Executor executors;
    protected DataConfigurationProperties.ElasticsearchProperties elasticProperties;
    protected IPetriNetService petriNetService;
    protected IWorkflowService workflowService;
    protected IElasticCasePrioritySearch iElasticCasePrioritySearch;
    protected ApplicationEventPublisher publisher;
    protected ElasticQueueManager caseElasticIndexQueueManager;
    protected ElasticQueueManager caseElasticDeleteQueueManager;

    public ElasticCaseService(ElasticCaseRepository repository,
                              ElasticsearchTemplate template,
                              Executor executors,
                              DataConfigurationProperties.ElasticsearchProperties elasticProperties,
                              @Lazy IPetriNetService petriNetService,
                              @Lazy IWorkflowService workflowService,
                              IElasticCasePrioritySearch iElasticCasePrioritySearch,
                              ApplicationEventPublisher publisher,
                              ElasticsearchClient elasticsearchClient) {
        this.repository = repository;
        this.template = template;
        this.executors = executors;
        this.elasticProperties = elasticProperties;
        this.petriNetService = petriNetService;
        this.workflowService = workflowService;
        this.iElasticCasePrioritySearch = iElasticCasePrioritySearch;
        this.publisher = publisher;
        this.caseElasticIndexQueueManager = new ElasticQueueManager(elasticProperties, elasticsearchClient, publisher);
        this.caseElasticDeleteQueueManager = new ElasticQueueManager(elasticProperties, elasticsearchClient, publisher);

    }

    @PreDestroy
    private void stopQueues() {
        caseElasticIndexQueueManager.shutdown();
        caseElasticDeleteQueueManager.shutdown();
        log.info("Queues for cases have been stopped");
    }

    @Override
    public void remove(String caseId) {
        caseElasticDeleteQueueManager.push(new BulkOperationWrapper(
                BulkOperation.of(op -> op.delete(d -> d.index(elasticProperties.getIndex().get(DataConfigurationProperties.ElasticsearchProperties.CASE_INDEX)).id(caseId))),
                null
        ));
        log.info("[{}]: Case \"{}\" queued for deletion", caseId, caseId);
    }

    @Override
    public void index(ElasticCase useCase) {
        Optional<com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticCase> elasticCaseOptional = repository.findById(useCase.getId());
        if (elasticCaseOptional.isEmpty()) {
            caseElasticIndexQueueManager.push(BulkOperationWrapper.builder()
                    .operation(createIndexOperation(useCase))
                    .publishableEvent(new IndexCaseEvent(useCase))
                    .build());
        } else {
            com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticCase elasticCase = elasticCaseOptional.get();
            elasticCase.update(useCase);
            caseElasticIndexQueueManager.push(BulkOperationWrapper.builder()
                    .operation(createIndexOperation(elasticCase))
                    .publishableEvent(new IndexCaseEvent(elasticCase))
                    .build());
        }
        log.debug("[{}]: Case \"{}\" queued for indexing", useCase.getId(), useCase.getTitle());
    }

    @Override
    public void indexNow(ElasticCase useCase) {
        index(useCase);
    }

    @Override
    public Page<Case> search(List<CaseSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        if (requests == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }
        log.debug("Searching for query with logged user [{}]", user.getId());
        // TODO: impersonation
//        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        LoggedUser loggedOrImpersonated = user;
//        pageable = resolveUnmappedSortAttributes(pageable);
        NativeQuery query = buildQuery(requests, loggedOrImpersonated, pageable, locale, isIntersection);
        List<Case> casePage;
        long total;
        if (query != null) {
            SearchHits<ElasticCase> hits = template.search(query, ElasticCase.class, IndexCoordinates.of(elasticProperties.getIndex().get(DataConfigurationProperties.ElasticsearchProperties.CASE_INDEX)));
            Page<ElasticCase> indexedCases = (Page) SearchHitSupport.unwrapSearchHits(SearchHitSupport.searchPageFor(hits, query.getPageable()));
            casePage = workflowService.findAllById(indexedCases.get().map(ElasticCase::getId).collect(Collectors.toList()));
            total = indexedCases.getTotalElements();
            log.debug("Found [{}] total elements of page [{}]", casePage.size(), pageable.getPageNumber());
        } else {
            casePage = Collections.emptyList();
            total = 0;
        }

        return new PageImpl<>(casePage, pageable, total);
    }

    @Override
    public long count(List<CaseSearchRequest> requests, LoggedUser user, Locale locale, Boolean isIntersection) {
        if (requests == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        // TODO: impersonation
//        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        LoggedUser loggedOrImpersonated = user;
        NativeQuery query = buildQuery(requests, loggedOrImpersonated, new FullPageRequest(), locale, isIntersection);
        if (query != null) {
            return template.count(query, com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticCase.class);
        } else {
            return 0;
        }
    }

    protected NativeQuery buildQuery(List<CaseSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        List<BoolQuery.Builder> singleQueries = requests.stream().map(request -> buildSingleQuery(request, user, locale)).collect(Collectors.toList());

        if (isIntersection && !singleQueries.stream().allMatch(Objects::nonNull)) {
            // one of the queries evaluates to empty set => the entire result is an empty set
            return null;
        } else if (!isIntersection) {
            singleQueries = singleQueries.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (singleQueries.isEmpty()) {
                // all queries result in an empty set => the entire result is an empty set
                return null;
            }
        }

        BinaryOperator<BoolQuery.Builder> reductionOperation = isIntersection ? (a, b) -> a.must(b.build()._toQuery()) : (a, b) -> a.should(b.build()._toQuery());
        BoolQuery.Builder query = singleQueries.stream().reduce(new BoolQuery.Builder(), reductionOperation);

        NativeQueryBuilder builder = new NativeQueryBuilder()
                .withQuery(query.build()._toQuery())
                .withPageable(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));

        for (org.springframework.data.domain.Sort.Order o : pageable.getSort()) {
            builder.withSort(s -> s.field(f -> f
                    .field(o.getProperty())
                    .order(o.isAscending()
                            ? co.elastic.clients.elasticsearch._types.SortOrder.Asc
                            : co.elastic.clients.elasticsearch._types.SortOrder.Desc)
                    .unmappedType(FieldType.Keyword)
                    .missing("_last")
            ));
        }

        return builder.build();
    }

    protected BoolQuery.Builder buildSingleQuery(CaseSearchRequest request, LoggedUser user, Locale locale) {
        BoolQuery.Builder query = new BoolQuery.Builder();

        buildViewPermissionQuery(query, user);
        buildPetriNetQuery(request, user, query);
        buildAuthorQuery(request, query);
        buildTaskQuery(request, query);
        buildRoleQuery(request, query);
        buildDataQuery(request, query);
        buildFullTextQuery(request, query);
        buildStringQuery(request, query, user);
        buildCaseIdQuery(request, query);
        buildUriNodeIdQuery(request, query);
        buildTagsQuery(request, query);
        boolean resultAlwaysEmpty = buildGroupQuery(request, user, locale, query);

        // TODO: filtered query https://stackoverflow.com/questions/28116404/filtered-query-using-nativesearchquerybuilder-in-spring-data-elasticsearch

        return resultAlwaysEmpty ? null : query;
    }

    protected void buildPetriNetQuery(CaseSearchRequest request, LoggedUser user, BoolQuery.Builder query) {
        if (request.process == null || request.process.isEmpty()) {
            return;
        }

        Set<String> identifiersSet = new HashSet<>();
        Set<String> processIdsSet = new HashSet<>();

        request.process.forEach(p -> {
            if (p.identifier != null) {
                identifiersSet.add(p.identifier);
            }
            if (p.processId != null) {
                processIdsSet.add(p.processId);
            }
        });
        TermsQueryField identifiers = new TermsQueryField.Builder()
                .value(identifiersSet.stream().map(FieldValue::of).collect(Collectors.toList()))
                .build();

        TermsQueryField processIds = new TermsQueryField.Builder()
                .value(processIdsSet.stream().map(FieldValue::of).collect(Collectors.toList()))
                .build();

        BoolQuery.Builder petriNetQuery = new BoolQuery.Builder();
        if (!identifiers.value().isEmpty()) {
            petriNetQuery.should(QueryBuilders.terms(term -> term.field("processIdentifier").terms(identifiers)));
        }
        if (!processIds.value().isEmpty()) {
            petriNetQuery.should(QueryBuilders.terms(term -> term.field("processId").terms(processIds)));
        }

        query.filter(petriNetQuery.build()._toQuery());
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
    protected void buildAuthorQuery(CaseSearchRequest request, BoolQuery.Builder query) {
        if (request.author == null || request.author.isEmpty()) {
            return;
        }

        BoolQuery.Builder authorsQuery = new BoolQuery.Builder();
        for (CaseSearchRequest.Author author : request.author) {
            BoolQuery.Builder authorQuery = new BoolQuery.Builder();
            if (author.email != null) {
                authorQuery.must(termQuery("authorEmail", author.email)._toQuery());
            }
            if (author.id != null) {
                authorQuery.must(matchQuery("author", author.id, null, null)._toQuery());
            }
            if (author.name != null) {
                authorQuery.must(termQuery("authorName", author.name)._toQuery());
            }
            authorsQuery.should(authorQuery.build()._toQuery());
        }

        query.filter(authorsQuery.build()._toQuery());
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
    protected void buildTaskQuery(CaseSearchRequest request, BoolQuery.Builder query) {
        if (request.transition == null || request.transition.isEmpty()) {
            return;
        }

        TermsQueryField taskIds = new TermsQueryField.Builder()
                .value(request.transition.stream().map(FieldValue::of).collect(Collectors.toList()))
                .build();

        query.filter(QueryBuilders.terms(term -> term.field("taskIds").terms(taskIds)));
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
    protected void buildRoleQuery(CaseSearchRequest request, BoolQuery.Builder query) {
        if (request.role == null || request.role.isEmpty()) {
            return;
        }

        TermsQueryField roleIds = new TermsQueryField.Builder()
                .value(request.role.stream().map(FieldValue::of).collect(Collectors.toList()))
                .build();

        query.filter(QueryBuilders.terms(term -> term.field("enabledRoles").terms(roleIds)));
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
    protected void buildDataQuery(CaseSearchRequest request, BoolQuery.Builder query) {
        if (request.data == null || request.data.isEmpty()) {
            return;
        }

        BoolQuery.Builder dataQuery = new BoolQuery.Builder();
        for (Map.Entry<String, String> field : request.data.entrySet()) {
            if (field.getKey().contains("."))
                dataQuery.must(termQuery("dataSet." + field.getKey(), field.getValue())._toQuery());
            else
                dataQuery.must(termQuery("dataSet." + field.getKey() + ".fulltextValue.keyword", field.getValue())._toQuery());
        }

        query.filter(dataQuery.build()._toQuery());
    }

    protected void buildTagsQuery(CaseSearchRequest request, BoolQuery.Builder query) {
        if (request.tags == null || request.tags.isEmpty()) {
            return;
        }

        BoolQuery.Builder tagsQuery = new BoolQuery.Builder();
        for (Map.Entry<String, String> field : request.tags.entrySet()) {
            tagsQuery.must(termQuery("tags." + field.getKey(), field.getValue())._toQuery());
        }

        query.filter(tagsQuery.build()._toQuery());
    }

    protected void buildFullTextQuery(CaseSearchRequest request, BoolQuery.Builder query) {
        if (request.fullText == null || request.fullText.isEmpty()) {
            return;
        }

        // TODO: improvement? wildcard does not scale good
        //String searchText = elasticsearchProperties.isAnalyzerEnabled() ? request.fullText : "*" + request.fullText + "*";
        String searchText = "*" + request.fullText + "*";
        QueryStringQuery fullTextQuery = QueryStringQuery.of(builder -> builder.fields(iElasticCasePrioritySearch.fullTextFields()).query(searchText));
        query.must(fullTextQuery._toQuery());
    }

    /**
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html">Query String Query</a>
     */
    protected void buildStringQuery(CaseSearchRequest request, BoolQuery.Builder query, LoggedUser user) {
        if (request.query == null || request.query.isEmpty()) {
            return;
        }

        String populatedQuery = request.query.replaceAll(ElasticQueryConstants.USER_ID_TEMPLATE, user.getId().toString());

        query.must(QueryStringQuery.of(builder -> builder.query(populatedQuery).allowLeadingWildcard(true).analyzeWildcard(true))._toQuery());
    }

    /**
     * Case with id "5cb07b6ff05be15f0b972c36"
     * <pre>
     * {
     *     "id": "5cb07b6ff05be15f0b972c36"
     * }
     * </pre>
     * <p>
     * Cases with id "5cb07b6ff05be15f0b972c36" OR "5cb07b6ff05be15f0b972c31"
     * <pre>
     * {
     *     "id" [
     *         "5cb07b6ff05be15f0b972c36",
     *         "5cb07b6ff05be15f0b972c31"
     *     ]
     * }
     * </pre>
     */

    protected void buildCaseIdQuery(CaseSearchRequest request, BoolQuery.Builder query) {
        List<String> validIds = Stream.concat(
                Optional.ofNullable(request.stringId).orElse(Collections.emptyList()).stream(),
                Optional.ofNullable(request.id).orElse(Collections.emptyList()).stream()
        ).toList();

        if (validIds.isEmpty()) {
            return;
        }

        TermsQueryField ids = new TermsQueryField.Builder()
                .value(validIds.stream().map(FieldValue::of).collect(Collectors.toList()))
                .build();

        query.filter(QueryBuilders.terms(term -> term.field("_id").terms(ids)));
    }

    protected void buildUriNodeIdQuery(CaseSearchRequest request, BoolQuery.Builder query) {
        if (request.uriNodeId == null || request.uriNodeId.isEmpty()) {
            return;
        }

        query.filter(termQuery("uriNodeId", request.uriNodeId)._toQuery());
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
    protected boolean buildGroupQuery(CaseSearchRequest request, LoggedUser user, Locale locale, BoolQuery.Builder query) {
        if (request.group == null || request.group.isEmpty()) {
            return false;
        }

        PetriNetSearch processQuery = new PetriNetSearch();
        processQuery.setGroup(request.group);
        List<PetriNetReference> groupProcesses = this.petriNetService.search(processQuery, user, new FullPageRequest(), locale).getContent();
        if (groupProcesses.isEmpty()) {
            return true;
        }

        TermsQueryField stringIds = new TermsQueryField.Builder()
                .value(groupProcesses.stream().map(PetriNetReference::getIdentifier).map(FieldValue::of).collect(Collectors.toList()))
                .build();

        query.filter(QueryBuilders.terms(term -> term.field("processIdentifier").terms(stringIds)));
        return false;
    }

    protected Pageable resolveUnmappedSortAttributes(Pageable pageable) {
        List<Sort.Order> modifiedOrders = new ArrayList<>();
        pageable.getSort().iterator().forEachRemaining(order -> modifiedOrders.add(new Order(order.getDirection(), order.getProperty()).withUnmappedType("keyword")));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()).withSort(Sort.by(modifiedOrders));
    }

    private BulkOperation createIndexOperation(ElasticCase useCase) {
        return BulkOperation.of(op -> op.index(i -> i
                .index(elasticProperties.getIndex().get(DataConfigurationProperties.ElasticsearchProperties.CASE_INDEX))
                .id(useCase.getId())
                .document(template.getElasticsearchConverter().mapObject(useCase))));
    }
}
