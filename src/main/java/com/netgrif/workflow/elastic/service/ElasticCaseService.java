package com.netgrif.workflow.elastic.service;

import com.google.common.collect.ImmutableMap;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.domain.ElasticCaseRepository;
import com.netgrif.workflow.elastic.service.executors.Executor;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.workflow.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.utils.FullPageRequest;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class ElasticCaseService implements IElasticCaseService {

    private static final Logger log = LoggerFactory.getLogger(ElasticCaseService.class);

    private ElasticCaseRepository repository;
    private IWorkflowService workflowService;
    private ElasticsearchTemplate template;
    private Executor executors;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    public ElasticCaseService(ElasticCaseRepository repository, ElasticsearchTemplate template, Executor executors) {
        this.repository = repository;
        this.template = template;
        this.executors = executors;
    }

    @Autowired
    @Lazy
    public void setWorkflowService(IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    private Map<String, Float> fullTextFieldMap = ImmutableMap.of(
            "title", 2f,
            "authorName", 1f,
            "authorEmail", 1f
    );

    /**
     * See {@link QueryStringQueryBuilder#fields(Map)}
     *
     * @return map where keys are ElasticCase field names and values are boosts of these fields
     */
    @Override
    public Map<String, Float> fullTextFields() {
        return fullTextFieldMap;
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

        SearchQuery query = buildQuery(requests, user, pageable, locale, isIntersection);
        List<Case> casePage;
        long total;
        if (query != null) {
            Page<ElasticCase> indexedCases = repository.search(query);
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

        SearchQuery query = buildQuery(requests, user, new FullPageRequest(), locale, isIntersection);
        if (query != null) {
            return template.count(query, ElasticCase.class);
        } else {
            return 0;
        }
    }

    private SearchQuery buildQuery(List<CaseSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
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

        buildPetriNetQuery(request, user, query);
        buildAuthorQuery(request, query);
        buildTaskQuery(request, query);
        buildRoleQuery(request, query);
        buildDataQuery(request, query);
        buildFullTextQuery(request, query);
        buildStringQuery(request, query);
        buildCaseIdQuery(request, query);
        boolean resultAlwaysEmpty = buildGroupQuery(request, user, locale, query);

        // TODO: filtered query https://stackoverflow.com/questions/28116404/filtered-query-using-nativesearchquerybuilder-in-spring-data-elasticsearch

        if (resultAlwaysEmpty)
            return null;
        else
            return query;
    }

    /**
     * Cases with processIdentifier "id" <br>
     * <pre>
     * {
     *     "petriNet": {
     *         "identifier": "id"
     *     }
     * }</pre><br>
     * <p>
     * Cases with processIdentifiers "1" OR "2" <br>
     * <pre>
     * {
     *     "petriNet": [
     *         {
     *             "identifier": "1"
     *         },
     *         {
     *             "identifier": "2"
     *         }
     *     ]
     * }
     * </pre>
     */
    private void buildPetriNetQuery(CaseSearchRequest request, LoggedUser user, BoolQueryBuilder query) {
        if (request.petriNet == null || request.petriNet.isEmpty()) {
            return;
        }

        BoolQueryBuilder petriNetQuery = boolQuery();

        for (CaseSearchRequest.PetriNet petriNet : request.petriNet) {
            if (petriNet.identifier != null) {
                petriNetQuery.should(termQuery("processIdentifier", petriNet.identifier));
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
     * Cases where "text_field" has value "text" AND "number_field" has value 125.<br>
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
            dataQuery.must(matchQuery("dataSet." + field.getKey() + ".value", field.getValue()));
        }

        query.filter(dataQuery);
    }

    /**
     * Full text search on fields defined by {@link #fullTextFields()}.
     */
    private void buildFullTextQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.fullText == null || request.fullText.isEmpty()) {
            return;
        }

        // TODO: improvement? wildcard does not scale good
        QueryBuilder fullTextQuery = queryStringQuery("*" + request.fullText + "*").fields(fullTextFields());
        query.must(fullTextQuery);
    }

    /**
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html">Query String Query</a>
     */
    private void buildStringQuery(CaseSearchRequest request, BoolQueryBuilder query) {
        if (request.query == null || request.query.isEmpty()) {
            return;
        }

        query.must(queryStringQuery(request.query));
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
        if(request.stringId == null || request.stringId.isEmpty()) {
            return;
        }

        BoolQueryBuilder caseIdQuery = boolQuery();
        request.stringId.forEach(caseId -> caseIdQuery.should(termQuery("stringId", caseId)));
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
}