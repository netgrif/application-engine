package com.netgrif.workflow.elastic.service;

import com.google.common.collect.ImmutableMap;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticTask;
import com.netgrif.workflow.elastic.domain.ElasticTaskRepository;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.workflow.utils.FullPageRequest;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.web.requestbodies.TaskSearchRequest;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@Service
public class ElasticTaskService implements IElasticTaskService {

    private static final Logger log = LoggerFactory.getLogger(ElasticTaskService.class);

    private ElasticTaskRepository repository;
    private ITaskService taskService;
    private ElasticsearchTemplate template;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    public ElasticTaskService(ElasticTaskRepository repository, ElasticsearchTemplate template) {
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
            repository.deleteAllByStringId(taskId);
            log.info("[?]: Task \"" + taskId + "\" deleted");
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
            }
        });
    }

    @Override
    public void indexNow(ElasticTask task) {
        index(task);
    }

    @Override
    public Page<Task> search(List<ElasticTaskSearchRequest> requests, LoggedUser user, Pageable pageable, Boolean isIntersection) {
        SearchQuery query = buildQuery(requests, user, pageable, isIntersection);
        Page<ElasticTask> indexedTasks = repository.search(query);
        List<Task> taskPage = taskService.findAllById(indexedTasks.get().map(ElasticTask::getStringId).collect(Collectors.toList()));

        return new PageImpl<>(taskPage, pageable, indexedTasks.getTotalElements());
    }

    @Override
    public long count(List<ElasticTaskSearchRequest> requests, LoggedUser user, Boolean isIntersection) {
        SearchQuery query = buildQuery(requests, user, new FullPageRequest(), isIntersection);

        return template.count(query, ElasticTask.class);
    }

    private SearchQuery buildQuery(List<ElasticTaskSearchRequest> requests, LoggedUser user, Pageable pageable, Boolean isIntersection) {
        BinaryOperator<BoolQueryBuilder> reductionOperator = isIntersection ? BoolQueryBuilder::must : BoolQueryBuilder::should;

        BoolQueryBuilder query = requests.stream()
                .map(request -> buildSingleQuery(request, user))
                .reduce(new BoolQueryBuilder(), reductionOperator);

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        return builder
                .withQuery(query)
                .withPageable(pageable)
                .build();
    }

    private BoolQueryBuilder buildSingleQuery(ElasticTaskSearchRequest request, LoggedUser user) {
        if (request == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }
        addRolesQueryConstraint(request, user);

        BoolQueryBuilder query = boolQuery();

        buildRoleQuery(request, query);
        buildCaseQuery(request, query);
        buildTitleQuery(request, query);
        buildUserQuery(request, query);
        buildProcessQuery(request, query);
        buildFullTextQuery(request, query);
        buildTransitionQuery(request, query);
        buildStringQuery(request, query);

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
     * Tasks with role "5cb07b6ff05be15f0b972c31"
     * {
     * "role": "5cb07b6ff05be15f0b972c31"
     * }
     * <p>
     * Tasks with role "5cb07b6ff05be15f0b972c31" OR "5cb07b6ff05be15f0b972c36"
     * {
     * "role": [
     * "5cb07b6ff05be15f0b972c31",
     * "5cb07b6ff05be15f0b972c36"
     * ]
     * }
     */
    private void buildRoleQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.role == null || request.role.isEmpty()) {
            return;
        }

        BoolQueryBuilder roleQuery = boolQuery();
        for (String roleId : request.role) {
            roleQuery.should(termQuery("roles", roleId));
        }

        query.filter(roleQuery);
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
    private QueryBuilder caseRequestQuery(TaskSearchRequest.TaskSearchCaseRequest caseRequest) {
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
        for (Long user : request.user) {
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
        for (String process : request.process) {
            processQuery.should(termQuery("processId", process));
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
    private void buildStringQuery(ElasticTaskSearchRequest request, BoolQueryBuilder query) {
        if (request.query == null || request.query.isEmpty()) {
            return;
        }

        query.must(queryStringQuery(request.query));
    }
}