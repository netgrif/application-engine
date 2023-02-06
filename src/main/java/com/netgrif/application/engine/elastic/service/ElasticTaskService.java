package com.netgrif.application.engine.elastic.service;

import com.google.common.collect.ImmutableMap;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.domain.ElasticQueryConstants;
import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet;
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.TaskSearchCaseRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class ElasticTaskService extends ElasticViewPermissionService implements IElasticTaskService {

    private static final Logger log = LoggerFactory.getLogger(ElasticTaskService.class);

    private ElasticTaskRepository repository;
    private ITaskService taskService;
    private ElasticsearchRestTemplate template;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Value("${spring.data.elasticsearch.index.task}")
    private String taskIndex;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    public ElasticTaskService(ElasticTaskRepository repository, ElasticsearchRestTemplate template) {
        this.repository = repository;
        this.template = template;
    }

    @Autowired
    @Lazy
    public void setTaskService(ITaskService taskService) {
        this.taskService = taskService;
    }

    private Map<String, Float> fullTextFieldMap = ImmutableMap.of(
            "title", 1f,
            "caseTitle", 1f
    );

    private Map<String, Float> caseTitledMap = ImmutableMap.of(
            "caseTitle", 1f
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
    public void remove(String taskId) {
        executor.execute(() -> {
            try {
                repository.deleteAllByStringId(taskId);
                log.info("[?]: Task \"" + taskId + "\" deleted");
            } catch (RuntimeException e) {
                log.error("Elastic executor was killed before finish: " + e.getMessage());
            }
        });
    }

    @Override
    public void removeByPetriNetId(String petriNetId) {
        executor.execute(() -> {
            try {
                repository.deleteAllByProcessId(petriNetId);
                log.info("[" + petriNetId + "]: All tasks of Petri Net with id \"" + petriNetId + "\" deleted");
            } catch (RuntimeException e) {
                log.error("Elastic executor was killed before finish: " + e.getMessage());
            }
        });
    }

    @Async
    @Override
    public void index(ElasticTask task) {
        executor.execute(() -> {
            try {
                ElasticTask elasticTask = repository.findByStringId(task.getStringId());

                if (elasticTask == null) {
                    repository.save(task);
                } else {
                    elasticTask.update(task);
                    repository.save(elasticTask);
                }

                log.debug("[" + task.getCaseId() + "]: Task \"" + task.getTitle() + "\" [" + task.getStringId() + "] indexed");
            } catch (InvalidDataAccessApiUsageException e) {
                log.debug("[" + task.getCaseId() + "]: Task \"" + task.getTitle() + "\" has duplicates, will be reindexed");
                repository.deleteAllByStringId(task.getStringId());
                repository.save(task);
                log.debug("[" + task.getCaseId() + "]: Task \"" + task.getTitle() + "\" indexed");
            } catch (RuntimeException e) {
                log.error("Elastic executor was killed before finish: " + e.getMessage());
            }
        });
    }

    @Override
    public void indexNow(ElasticTask task) {
        index(task);
    }

    @Override
    public Page<Task> search(List<ElasticTaskSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        NativeSearchQuery query = buildQuery(requests, user.getSelfOrImpersonated(), pageable, locale, isIntersection);
        List<Task> taskPage;
        long total;
        if (query != null) {
            SearchHits<ElasticTask> hits = elasticsearchTemplate.search(query, ElasticTask.class,  IndexCoordinates.of(taskIndex));
            Page<ElasticTask> indexedTasks = (Page)SearchHitSupport.unwrapSearchHits(SearchHitSupport.searchPageFor(hits, query.getPageable()));
            taskPage = taskService.findAllById(indexedTasks.get().map(ElasticTask::getStringId).collect(Collectors.toList()));
            total = indexedTasks.getTotalElements();
        } else {
            taskPage = Collections.emptyList();
            total = 0;
        }

        return new PageImpl<>(taskPage, pageable, total);
    }

    @Override
    public long count(List<ElasticTaskSearchRequest> requests, LoggedUser user, Locale locale, Boolean isIntersection) {
        NativeSearchQuery query = buildQuery(requests, user.getSelfOrImpersonated(), new FullPageRequest(), locale, isIntersection);
        if (query != null) {
            return template.count(query, ElasticTask.class);
        } else {
            return 0;
        }
    }

    private NativeSearchQuery buildQuery(List<ElasticTaskSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
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

    private BoolQueryBuilder buildSingleQuery(ElasticTaskSearchRequest request, LoggedUser user, Locale locale) {
        if (request == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }
        addRolesQueryConstraint(request, user);

        BoolQueryBuilder query = boolQuery();
        buildViewPermissionQuery(query, user);
        buildCaseQuery(request, query);
        buildTitleQuery(request, query);
        buildUserQuery(request, query);
        buildProcessQuery(request, query);
        buildFullTextQuery(request, query);
        buildTransitionQuery(request, query);
        buildStringQuery(request, query, user);
        boolean resultAlwaysEmpty = buildGroupQuery(request, user, locale, query);

        if (resultAlwaysEmpty)
            return null;
        else
            return query;
    }

    protected void addRolesQueryConstraint(ElasticTaskSearchRequest request, LoggedUser user) {
        if (request.role != null && !request.role.isEmpty()) {
            Set<String> roles = new HashSet<>(request.role);
            roles.addAll(user.getProcessRoles());
            request.role = new ArrayList<>(roles);
        } else {
            request.role = new ArrayList<>(user.getProcessRoles());
        }
    }


    /**
     * Tasks of case with id "5cb07b6ff05be15f0b972c4d"
     * {
     * "case": {
     * "id": "5cb07b6ff05be15f0b972c4d"
     * }
     * }
     * <p>
     * Tasks of cases with id "5cb07b6ff05be15f0b972c4d" OR "5cb07b6ff05be15f0b972c4e"
     * {
     * "case": [{
     * "id": "5cb07b6ff05be15f0b972c4d"
     * },
     * {
     * "id": "5cb07b6ff05be15f0b972c4e"
     * }]
     * }
     * <p>
     * Tasks of case with case title containing "foo"
     * {
     * "case": {
     * "title": "foo"
     * }
     * }
     * <p>
     * Tasks of case with case title containing "foo" OR "bar"
     * {
     * "case": [{
     * "title": "foo"
     * },
     * {
     * "title: "bar"
     * }]
     * }
     */
    private void buildCaseQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.useCase == null || request.useCase.isEmpty()) {
            return;
        }

        BoolQueryBuilder casesQuery = boolQuery();
        request.useCase.stream().map(this::caseRequestQuery).filter(Objects::nonNull).forEach(casesQuery::should);

        query.filter(casesQuery);
    }

    /**
     * @return query for ID if only ID is present. Query for title if only title is present.
     * If both are present an ID query is returned. If neither are present null is returned.
     */
    private QueryBuilder caseRequestQuery(TaskSearchCaseRequest caseRequest) {
        if (caseRequest.id != null) {
            return termQuery("caseId", caseRequest.id);
        } else if (caseRequest.title != null) {
            return queryStringQuery("*" + caseRequest.title + "*").fields(this.caseTitledMap);
        }
        return null;
    }

    /**
     * Tasks with title (default value) "New task"
     * {
     * "title": "New task"
     * }
     * <p>
     * Tasks with title (default value) "New task" OR "Status"
     * {
     * "title": [
     * "New task",
     * "Status"
     * ]
     * }
     */
    private void buildTitleQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.title == null || request.title.isEmpty()) {
            return;
        }

        BoolQueryBuilder titleQuery = boolQuery();
        for (String title : request.title) {
            titleQuery.should(termQuery("title", title));
        }

        query.filter(titleQuery);
    }

    /**
     * Tasks assigned to user with id 1
     * {
     * "user": 1
     * }
     * <p>
     * Tasks assigned to user with id 1 OR 2
     */
    private void buildUserQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.user == null || request.user.isEmpty()) {
            return;
        }

        BoolQueryBuilder userQuery = boolQuery();
        for (String user : request.user) {
            userQuery.should(termQuery("userId", user));
        }

        query.filter(userQuery);
    }

    /**
     * Tasks of process "document"
     * {
     * "process": "document"
     * }
     * <p>
     * Tasks of process "document" OR "folder"
     * {
     * "process": [
     * "document",
     * "folder",
     * ]
     * }
     */
    private void buildProcessQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.process == null || request.process.isEmpty()) {
            return;
        }

        BoolQueryBuilder processQuery = boolQuery();
        for (PetriNet process : request.process) {
            if (process.identifier != null) {
                processQuery.should(termQuery("processId", process.identifier));
            }
        }

        query.filter(processQuery);
    }

    /**
     * Full text search on fields defined by {@link #fullTextFields()}.
     */
    private void buildFullTextQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.fullText == null || request.fullText.isEmpty()) {
            return;
        }

        QueryBuilder fullTextQuery = queryStringQuery("*" + request.fullText + "*").fields(fullTextFields());
        query.must(fullTextQuery);
    }

    /**
     * Tasks with transition id "document"
     * {
     * "transitionId": "document"
     * }
     * <p>
     * Tasks with transition id "document" OR "folder"
     * {
     * "transitionId": [
     * "document",
     * "folder",
     * ]
     * }
     */
    private void buildTransitionQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.transitionId == null || request.transitionId.isEmpty()) {
            return;
        }

        BoolQueryBuilder transitionQuery = boolQuery();
        request.transitionId.forEach(transitionId -> transitionQuery.should(termQuery("transitionId", transitionId)));

        query.filter(transitionQuery);
    }

    /**
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html">Query String Query</a>
     */
    private void buildStringQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query, LoggedUser user) {
        if (request.query == null || request.query.isEmpty()) {
            return;
        }

        String populatedQuery = request.query.replaceAll(ElasticQueryConstants.USER_ID_TEMPLATE, user.getId().toString());

        query.must(queryStringQuery(populatedQuery));
    }

    /**
     * Tasks of cases of group with id "5cb07b6ff05be15f0b972c4d"
     * {
     * "group": "5cb07b6ff05be15f0b972c4d"
     * }
     * <p>
     * Tasks of cases of group with id "5cb07b6ff05be15f0b972c4d" OR "5cb07b6ff05be15f0b972c4e"
     * {
     * "transitionId": [
     * "5cb07b6ff05be15f0b972c4d",
     * "5cb07b6ff05be15f0b972c4e",
     * ]
     * }
     */
    public boolean buildGroupQuery(TaskSearchRequest request, LoggedUser user, Locale locale, BoolQueryBuilder query) {
        if (request.group == null || request.group.isEmpty())
            return false;

        Map<String, Object> processQuery = new HashMap<>();
        processQuery.put("group", request.group);
        List<PetriNetReference> groupProcesses = this.petriNetService.search(processQuery, user, new FullPageRequest(), locale).getContent();
        if (groupProcesses.size() == 0)
            return true;

        BoolQueryBuilder groupProcessQuery = boolQuery();
        for (PetriNetReference process : groupProcesses) {
            groupProcessQuery.should(termQuery("processId", process.getStringId()));
        }

        query.filter(groupProcessQuery);
        return false;
    }
}