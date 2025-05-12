package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.elastic.domain.ElasticJob;
import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.domain.ElasticTaskJob;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskSearchService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.service.query.ElasticPermissionQueryBuilder;
import com.netgrif.application.engine.elastic.service.query.ElasticTaskQueryBuilder;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticTaskService implements IElasticTaskService {

    private final IElasticTaskSearchService searchService;
    private final ElasticTaskQueueManager elasticTaskQueueManager;
    private final ElasticTaskQueryBuilder queryBuilder;
    private final ElasticPermissionQueryBuilder permissionQueryBuilder;

    /**
     * See {@link QueryStringQueryBuilder#fields(Map)}
     *
     * @return map where keys are ElasticCase field names and values are boosts of these fields
     */
    @Override
    public Map<String, Float> fullTextFields() {
        return queryBuilder.fullTextFields();
    }

    @Override
    public Future<ElasticTask> scheduleTaskIndexing(ElasticTask task) {
        return elasticTaskQueueManager.scheduleOperation(new ElasticTaskJob(ElasticJob.INDEX, task));
    }

    @Override
    public void index(ElasticTask task) {
        elasticTaskQueueManager.scheduleOperation(new ElasticTaskJob(ElasticJob.INDEX, task));
    }

    @Override
    public void indexNow(ElasticTask task) {
        index(task);
    }

    @Override
    public Page<Task> search(List<ElasticTaskSearchRequest> requests, String actorId, Pageable pageable,
                             Locale locale, Boolean isIntersection) {
        BoolQueryBuilder permissionQuery = permissionQueryBuilder.buildSingleQuery(actorId);
        return searchService.search(requests, actorId, pageable, locale, isIntersection, permissionQuery);
    }

    @Override
    public long count(List<ElasticTaskSearchRequest> requests, String actorId, Locale locale, Boolean isIntersection) {
        BoolQueryBuilder permissionQuery = permissionQueryBuilder.buildSingleQuery(actorId);
        return searchService.count(requests, actorId, locale, isIntersection, permissionQuery);
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
}
