package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.workflow.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;

public interface IElasticTaskService {

    Map<String, Float> fullTextFields();

    Future<ElasticTask> scheduleTaskIndexing(ElasticTask task);

    @Async
    void index(ElasticTask task);

    void indexNow(ElasticTask task);

    void remove(String taskId);

    void removeByPetriNetId(String petriNetId);

    Page<Task> search(List<ElasticTaskSearchRequest> requests, String actorId, Pageable pageable, Locale locale,
                      Boolean isIntersection);

    long count(List<ElasticTaskSearchRequest> requests, String actorId, Locale locale, Boolean isIntersection);
}