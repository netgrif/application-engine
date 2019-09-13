package com.netgrif.workflow.elastic.service;

import com.google.common.collect.ImmutableMap;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticTask;
import com.netgrif.workflow.elastic.domain.ElasticTaskRepository;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.elastic.web.TaskSearchRequest;
import com.netgrif.workflow.utils.FullPageRequest;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class ElasticTaskService implements IElasticTaskService {

    private static final Logger log = LoggerFactory.getLogger(ElasticCaseService.class);

    @Autowired
    private ElasticTaskRepository repository;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private ElasticsearchTemplate template;

//    @Autowired
//    private Executors executors;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Map<String, Float> fullTextFieldMap = ImmutableMap.of(
            "title", 1f,
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
            repository.deleteById(taskId);
            log.info("[?]: Task \"" + taskId + "\" deleted");
        });
    }

    @Async
    @Override
    public void index(Task task) {
        executor.execute(() -> {
            ElasticTask elasticTask = repository.findByStringId(task.getStringId());

            if (elasticTask == null ) {
                elasticTask = new ElasticTask(task);
            } else {
                elasticTask.update(task);
            }

            repository.save(elasticTask);

            log.debug("[" + task.getCaseId() + "]: Task \"" + task.getTitle() + "\" [" + task.getStringId() + "] indexed");
        });
    }

    @Override
    public void indexNow(Task task) {
        index(task);
    }

    @Override
    public Page<Task> search(TaskSearchRequest request, LoggedUser user, Pageable pageable) {
        if (request == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        SearchQuery query = buildQuery(request, user, pageable);
        Page<ElasticTask> indexedTasks = repository.search(query);
        List<Task> taskPage = taskService.findAllById(indexedTasks.get().map(ElasticTask::getStringId).collect(Collectors.toList()));

        return new PageImpl<>(taskPage, pageable, indexedTasks.getTotalElements());
    }

    @Override
    public long count(TaskSearchRequest request, LoggedUser user) {
        if (request == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        SearchQuery query = buildQuery(request, user, new FullPageRequest());

        return template.count(query, ElasticTask.class);
    }

    private SearchQuery buildQuery(TaskSearchRequest request, LoggedUser user, Pageable pageable) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        BoolQueryBuilder query = boolQuery();

        buildRoleQuery(request, query);
        buildCaseQuery(request, query);
        buildTitleQuery(request, query);
        buildUserQuery(request, query);
        buildProcessQuery(request, query);
        buildFullTextQuery(request, query);
        buildStringQuery(request, query);

        return builder
                .withQuery(query)
                .withPageable(pageable)
                .build();
    }

    /**
     * Tasks with role "5cb07b6ff05be15f0b972c31"
     * {
     *     "role": "5cb07b6ff05be15f0b972c31"
     * }
     *
     * Tasks with role "5cb07b6ff05be15f0b972c31" OR "5cb07b6ff05be15f0b972c36"
     * {
     *     "role": [
     *         "5cb07b6ff05be15f0b972c31",
     *         "5cb07b6ff05be15f0b972c36"
     *     ]
     * }
     */
    private void buildRoleQuery(TaskSearchRequest request, BoolQueryBuilder query) {
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
     *     "case": "5cb07b6ff05be15f0b972c4d"
     * }
     *
     * Tasks of cases with id "5cb07b6ff05be15f0b972c4d" OR "5cb07b6ff05be15f0b972c4e"
     * {
     *     "case": [
     *          "id": "5cb07b6ff05be15f0b972c4d",
     *          "id": "5cb07b6ff05be15f0b972c4e"
     *     ]
     * }
     */
    private void buildCaseQuery(TaskSearchRequest request, BoolQueryBuilder query) {
        if (request.useCase == null || request.useCase.isEmpty()) {
            return;
        }

        BoolQueryBuilder casesQuery = boolQuery();
        for (String caseId : request.useCase) {
            casesQuery.should(termQuery("caseId", caseId));
        }

        query.filter(casesQuery);
    }

    /**
     * Tasks with title (default value) "New task"
     * {
     *     "title": "New task"
     * }
     *
     * Tasks with title (default value) "New task" OR "Status"
     * {
     *     "title": [
     *         "New task",
     *         "Status"
     *     ]
     * }
     */
    private void buildTitleQuery(TaskSearchRequest request, BoolQueryBuilder query) {
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
     *     "user": 1
     * }
     *
     * Tasks assigned to user with id 1 OR 2
     */
    private void buildUserQuery(TaskSearchRequest request, BoolQueryBuilder query) {
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
     *     "process": "document"
     * }
     *
     * Tasks of process "document" OR "folder"
     * {
     *     "process": [
     *         "document",
     *         "folder",
     *     ]
     * }
     */
    private void buildProcessQuery(TaskSearchRequest request, BoolQueryBuilder query) {
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
    private void buildFullTextQuery(TaskSearchRequest request, BoolQueryBuilder query) {
        if (request.fullText == null || request.fullText.isEmpty()) {
            return;
        }

        QueryBuilder fullTextQuery = queryStringQuery("*" + request.fullText + "*").fields(fullTextFields());
        query.must(fullTextQuery);
    }

    /**
     * See <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html">Query String Query</a>
     */
    private void buildStringQuery(TaskSearchRequest request, BoolQueryBuilder query) {
        if (request.query == null || request.query.isEmpty()) {
            return;
        }

        query.must(queryStringQuery(request.query));
    }
}