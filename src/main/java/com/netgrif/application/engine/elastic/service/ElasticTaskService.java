package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.google.common.collect.ImmutableList;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.domain.ElasticJob;
import com.netgrif.application.engine.elastic.domain.ElasticQueryConstants;
import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.domain.ElasticTaskJob;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.event.events.task.IndexTaskEvent;
import com.netgrif.application.engine.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.PetriNet;
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.TaskSearchCaseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.springframework.data.elasticsearch.client.elc.Queries.termQuery;

@Service
public class ElasticTaskService extends ElasticViewPermissionService implements IElasticTaskService {

    private static final Logger log = LoggerFactory.getLogger(ElasticTaskService.class);

    protected ITaskService taskService;
    protected ElasticsearchTemplate template;

    @Value("${spring.data.elasticsearch.index.task}")
    protected String taskIndex;

    @Autowired
    protected ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    protected IPetriNetService petriNetService;

    @Autowired
    protected ApplicationEventPublisher publisher;

    protected List<String> fullTextFieldMap = ImmutableList.of(
            "title^1f",
            "caseTitle^1f"
    );

    protected List<String> caseTitledMap = ImmutableList.of(
            "caseTitle^1f"
    );

    @Autowired
    private ElasticTaskQueueManager elasticTaskQueueManager;

    @Autowired
    public ElasticTaskService(ElasticsearchTemplate template) {
        this.template = template;
    }

    @Autowired
    @Lazy
    public void setTaskService(ITaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * See {@link QueryStringQuery#fields()}
     *
     * @return map where keys are ElasticCase field names and values are boosts of these fields
     */
    @Override
    public List<String> fullTextFields() {
        return fullTextFieldMap;
    }

    @Override
    public void remove(String taskId) {
        ElasticTask task = new ElasticTask();
        task.setTaskId(taskId);
        elasticTaskQueueManager.scheduleOperation(new ElasticTaskJob(ElasticJob.REMOVE, task));
    }

    @Override
    public void removeByPetriNetId(String petriNetId) {
        elasticTaskQueueManager.removeTasksByProcess(petriNetId);
    }

    @Override
    public Future<ElasticTask> scheduleTaskIndexing(ElasticTask task) {
        return elasticTaskQueueManager.scheduleOperation(new ElasticTaskJob(ElasticJob.INDEX, task));
    }

    @Async
    @Override
    public void index(ElasticTask task) {
        Future<ElasticTask> taskFuture = elasticTaskQueueManager.scheduleOperation(new ElasticTaskJob(ElasticJob.INDEX, task));
        if (taskFuture instanceof CompletableFuture<ElasticTask>) {
            ((CompletableFuture<ElasticTask>) taskFuture).thenApply(elasticTask -> {
                publisher.publishEvent(new IndexTaskEvent(elasticTask));
                return null;
            });
        }
    }

    @Override
    public void indexNow(ElasticTask task) {
        index(task);
    }

    @Override
    public Page<Task> search(List<ElasticTaskSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        NativeQuery query = buildQuery(requests, user.getSelfOrImpersonated(), pageable, locale, isIntersection);
        List<Task> taskPage;
        long total;
        if (query != null) {
            SearchHits<ElasticTask> hits = elasticsearchTemplate.search(query, ElasticTask.class, IndexCoordinates.of(taskIndex));
            Page<ElasticTask> indexedTasks = (Page) SearchHitSupport.unwrapSearchHits(SearchHitSupport.searchPageFor(hits, query.getPageable()));
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
        NativeQuery query = buildQuery(requests, user.getSelfOrImpersonated(), new FullPageRequest(), locale, isIntersection);
        if (query != null) {
            return template.count(query, ElasticTask.class);
        } else {
            return 0;
        }
    }

    protected NativeQuery buildQuery(List<ElasticTaskSearchRequest> requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
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

    protected BoolQuery.Builder buildSingleQuery(ElasticTaskSearchRequest request, LoggedUser user, Locale locale) {
        if (request == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }
        addRolesQueryConstraint(request, user);

        BoolQuery.Builder query = new BoolQuery.Builder();
        buildViewPermissionQuery(query, user);
        buildCaseQuery(request, query);
        buildTitleQuery(request, query);
        buildUserQuery(request, query);
        buildProcessQuery(request, query);
        buildFullTextQuery(request, query);
        buildTransitionQuery(request, query);
        buildTagsQuery(request, query);
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
    protected void buildCaseQuery(ElasticTaskSearchRequest request, BoolQuery.Builder query) {
        if (request.useCase == null || request.useCase.isEmpty()) {
            return;
        }

        BoolQuery.Builder casesQuery = new BoolQuery.Builder();
        request.useCase.stream().map(this::caseRequestQuery).filter(Objects::nonNull).forEach(casesQuery::should);

        query.filter(casesQuery.build()._toQuery());
    }

    /**
     * @return query for ID if only ID is present. Query for title if only title is present.
     * If both are present an ID query is returned. If neither are present null is returned.
     */
    protected Query caseRequestQuery(TaskSearchCaseRequest caseRequest) {
        if (caseRequest.id != null) {
            return termQuery("caseId", caseRequest.id)._toQuery();
        } else if (caseRequest.title != null) {
            return QueryStringQuery.of(builder -> builder.fields(this.caseTitledMap).query("*" + caseRequest.title + "*"))._toQuery();
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
    protected void buildTitleQuery(ElasticTaskSearchRequest request, BoolQuery.Builder query) {
        if (request.title == null || request.title.isEmpty()) {
            return;
        }

        BoolQuery.Builder titleQuery = new BoolQuery.Builder();
        for (String title : request.title) {
            titleQuery.should(termQuery("title", title)._toQuery());
        }

        query.filter(titleQuery.build()._toQuery());
    }

    /**
     * Tasks assigned to user with id 1
     * {
     * "user": 1
     * }
     * <p>
     * Tasks assigned to user with id 1 OR 2
     */
    protected void buildUserQuery(ElasticTaskSearchRequest request, BoolQuery.Builder query) {
        if (request.user == null || request.user.isEmpty()) {
            return;
        }

        BoolQuery.Builder userQuery = new BoolQuery.Builder();
        for (String user : request.user) {
            userQuery.should(termQuery("userId", user)._toQuery());
        }

        query.filter(userQuery.build()._toQuery());
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
    protected void buildProcessQuery(ElasticTaskSearchRequest request, BoolQuery.Builder query) {
        if (request.process == null || request.process.isEmpty()) {
            return;
        }

        BoolQuery.Builder processQuery = new BoolQuery.Builder();
        for (PetriNet process : request.process) {
            if (process.identifier != null) {
                processQuery.should(termQuery("processId", process.identifier)._toQuery());
            }
        }

        query.filter(processQuery.build()._toQuery());
    }

    /**
     * Full text search on fields defined by {@link #fullTextFields()}.
     */
    protected void buildFullTextQuery(ElasticTaskSearchRequest request, BoolQuery.Builder query) {
        if (request.fullText == null || request.fullText.isEmpty()) {
            return;
        }

        QueryStringQuery fullTextQuery = QueryStringQuery.of(builder -> builder.fields(fullTextFields()).query("*" + request.fullText + "*"));
        query.must(fullTextQuery._toQuery());
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
    protected void buildTransitionQuery(ElasticTaskSearchRequest request, BoolQuery.Builder query) {
        if (request.transitionId == null || request.transitionId.isEmpty()) {
            return;
        }

        BoolQuery.Builder transitionQuery = new BoolQuery.Builder();
        request.transitionId.forEach(transitionId -> transitionQuery.should(termQuery("transitionId", transitionId)._toQuery()));

        query.filter(transitionQuery.build()._toQuery());
    }

    private void buildTagsQuery(ElasticTaskSearchRequest request, BoolQuery.Builder query) {
        if (request.tags == null || request.tags.isEmpty()) {
            return;
        }

        BoolQuery.Builder tagsQuery = new BoolQuery.Builder();
        for (Map.Entry<String, String> field : request.tags.entrySet()) {
            tagsQuery.must(termQuery("tags." + field.getKey(), field.getValue())._toQuery());
        }

        query.filter(tagsQuery.build()._toQuery());
    }

    /**
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html">Query String Query</a>
     */
    protected void buildStringQuery(ElasticTaskSearchRequest request, BoolQuery.Builder query, LoggedUser user) {
        if (request.query == null || request.query.isEmpty()) {
            return;
        }

        String populatedQuery = request.query.replaceAll(ElasticQueryConstants.USER_ID_TEMPLATE, user.getId());

        query.must(QueryStringQuery.of(builder -> builder.query(populatedQuery))._toQuery());
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
    public boolean buildGroupQuery(TaskSearchRequest request, LoggedUser user, Locale locale, BoolQuery.Builder query) {
        if (request.group == null || request.group.isEmpty())
            return false;

        PetriNetSearch processQuery = new PetriNetSearch();
        processQuery.setGroup(request.group);
        List<PetriNetReference> groupProcesses = this.petriNetService.search(processQuery, user, new FullPageRequest(), locale).getContent();
        if (groupProcesses.size() == 0)
            return true;

        BoolQuery.Builder groupProcessQuery = new BoolQuery.Builder();
        for (PetriNetReference process : groupProcesses) {
            groupProcessQuery.should(termQuery("processId", process.getStringId())._toQuery());
        }

        query.filter(groupProcessQuery.build()._toQuery());
        return false;
    }
}