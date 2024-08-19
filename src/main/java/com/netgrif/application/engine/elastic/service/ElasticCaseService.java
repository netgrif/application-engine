package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.domain.ElasticQueryConstants;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCasePrioritySearch;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Order;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.springframework.data.elasticsearch.client.elc.Queries.matchQuery;
import static org.springframework.data.elasticsearch.client.elc.Queries.termQuery;

//import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
@RequiredArgsConstructor
public class ElasticCaseService extends ElasticViewPermissionService implements IElasticCaseService {

    private static final Logger log = LoggerFactory.getLogger(ElasticCaseService.class);

    protected final ElasticCaseRepository repository;
    protected final ElasticsearchTemplate template;
    protected final Executor executors;
    protected final ElasticsearchProperties elasticsearchProperties;
    protected IPetriNetService petriNetService;
    protected IWorkflowService workflowService;
    protected IElasticCasePrioritySearch iElasticCasePrioritySearch;
    @Value("${spring.data.elasticsearch.index.case}")
    protected String caseIndex;

    @Autowired
    @Lazy
    public void setWorkflowService(IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Autowired
    @Lazy
    public void setPetriNetService(IPetriNetService petriNetService) {
        this.petriNetService = petriNetService;
    }

    @Autowired
    public void setElasticCasePrioritySearch(IElasticCasePrioritySearch iElasticCasePrioritySearch) {
        this.iElasticCasePrioritySearch = iElasticCasePrioritySearch;
    }

    @Override
    public void remove(String caseId) {
        executors.execute(caseId, () -> {
            repository.deleteAllByStringId(caseId);
            log.info("[" + caseId + "]: Case \"" + caseId + "\" deleted");
        });
    }

    @Override
    public void removeByPetriNetId(String processId) {
        executors.execute(processId, () -> {
            repository.deleteAllByProcessId(processId);
            log.info("[" + processId + "]: All cases of Petri Net with id \"" + processId + "\" deleted");
        });
    }

    @Override
    public void index(ElasticCase useCase) {
        executors.execute(useCase.getStringId(), () -> {
            try {
                ElasticCase elasticCase = repository.findByStringId(useCase.getStringId());
                if (elasticCase == null) {
                    repository.save(useCase);
                } else {
                    elasticCase.update(useCase);
                    repository.save(elasticCase);
                }
                log.debug("[" + useCase.getStringId() + "]: Case \"" + useCase.getTitle() + "\" indexed");
            } catch (InvalidDataAccessApiUsageException ignored) {
                log.debug("[" + useCase.getStringId() + "]: Case \"" + useCase.getTitle() + "\" has duplicates, will be reindexed");
                repository.deleteAllByStringId(useCase.getStringId());
                repository.save(useCase);
                log.debug("[" + useCase.getStringId() + "]: Case \"" + useCase.getTitle() + "\" indexed");
            }
        });
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

        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        pageable = resolveUnmappedSortAttributes(pageable);
        NativeQuery query = buildQuery(requests, loggedOrImpersonated, pageable, locale, isIntersection);
        List<Case> casePage;
        long total;
        if (query != null) {
            SearchHits<ElasticCase> hits = template.search(query, ElasticCase.class, IndexCoordinates.of(caseIndex));
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
    public long count(List<CaseSearchRequest> requests, LoggedUser user, Locale locale, Boolean isIntersection) {
        if (requests == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        NativeQuery query = buildQuery(requests, loggedOrImpersonated, new FullPageRequest(), locale, isIntersection);
        if (query != null) {
            return template.count(query, ElasticCase.class);
        } else {
            return 0;
        }
    }

    public String findUriNodeId(Case aCase) {
        if (aCase == null) {
            return null;
        }
        ElasticCase elasticCase = repository.findByStringId(aCase.getStringId());
        if (elasticCase == null) {
            log.warn("[" + aCase.getStringId() + "] Case with id [" + aCase.getStringId() + "] is not indexed.");
            return null;
        }

        return elasticCase.getUriNodeId();
    }

    protected NativeQuery buildQuery(List<CaseSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        List<BoolQuery.Builder> singleQueries = requests.stream().map(request -> buildSingleQuery(request, user, locale)).collect(Collectors.toList());

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

        BinaryOperator<BoolQuery.Builder> reductionOperation = isIntersection ? (a, b) -> a.must(b.build()._toQuery()) : (a, b) -> a.should(b.build()._toQuery());
        BoolQuery.Builder query = singleQueries.stream().reduce(new BoolQuery.Builder(), reductionOperation);

        NativeQueryBuilder builder = new NativeQueryBuilder();
        return builder
                .withQuery(query.build()._toQuery())
                .withPageable(pageable)
                .build();
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

        if (resultAlwaysEmpty)
            return null;
        else
            return query;
    }

    protected void buildPetriNetQuery(CaseSearchRequest request, LoggedUser user, BoolQuery.Builder query) {
        if (request.process == null || request.process.isEmpty()) {
            return;
        }

        BoolQuery.Builder petriNetQuery = new BoolQuery.Builder();

        for (CaseSearchRequest.PetriNet process : request.process) {
            if (process.identifier != null) {
                petriNetQuery.should(termQuery("processIdentifier", process.identifier)._toQuery());
            }
            if (process.processId != null) {
                petriNetQuery.should(termQuery("processId", process.processId)._toQuery());
            }
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

        BoolQuery.Builder taskQuery = new BoolQuery.Builder();
        for (String taskImportId : request.transition) {
            taskQuery.should(termQuery("taskIds", taskImportId)._toQuery());
        }

        query.filter(taskQuery.build()._toQuery());
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

        BoolQuery.Builder roleQuery = new BoolQuery.Builder();
        for (String roleId : request.role) {
            roleQuery.should(termQuery("enabledRoles", roleId)._toQuery());
        }

        query.filter(roleQuery.build()._toQuery());
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
    protected void buildCaseIdQuery(CaseSearchRequest request, BoolQuery.Builder query) {
        if (request.stringId == null || request.stringId.isEmpty()) {
            return;
        }

        BoolQuery.Builder caseIdQuery = new BoolQuery.Builder();
        request.stringId.forEach(caseId -> caseIdQuery.should(termQuery("stringId", caseId)._toQuery()));
        query.filter(caseIdQuery.build()._toQuery());
    }

    protected void buildUriNodeIdQuery(CaseSearchRequest request, BoolQuery.Builder query) {
        if (request.uriNodeId == null || request.uriNodeId.isEmpty()) {
            return;
        }

        BoolQuery.Builder caseIdQuery = new BoolQuery.Builder();
        caseIdQuery.should(termQuery("uriNodeId", request.uriNodeId)._toQuery());
        query.filter(caseIdQuery.build()._toQuery());
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
        if (groupProcesses.size() == 0)
            return true;

        BoolQuery.Builder groupQuery = new BoolQuery.Builder();
        groupProcesses.stream().map(PetriNetReference::getIdentifier)
                .map(netIdentifier -> termQuery("processIdentifier", netIdentifier))
                .forEach(termQuery -> groupQuery.should(termQuery._toQuery()));
        query.filter(groupQuery.build()._toQuery());
        return false;
    }

    protected Pageable resolveUnmappedSortAttributes(Pageable pageable) {
        List<Sort.Order> modifiedOrders = new ArrayList<>();
        pageable.getSort().iterator().forEachRemaining(order -> modifiedOrders.add(new Order(order.getDirection(), order.getProperty()).withUnmappedType("keyword")));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()).withSort(Sort.by(modifiedOrders));
    }
}