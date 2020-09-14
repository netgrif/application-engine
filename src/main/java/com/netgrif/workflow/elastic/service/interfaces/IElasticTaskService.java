package com.netgrif.workflow.elastic.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticTask;
import com.netgrif.workflow.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;

public interface IElasticTaskService {

    Map<String, Float> fullTextFields();

    @Async
    void index(ElasticTask task);

    void indexNow(ElasticTask task);

    Page<Task> search(List<ElasticTaskSearchRequest> requests, LoggedUser user, Pageable pageable, Boolean isIntersection);

    long count(List<ElasticTaskSearchRequest> requests, LoggedUser user, Boolean isIntersection);

    void remove(String taskId);
}