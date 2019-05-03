package com.netgrif.workflow.elastic.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticTask;
import com.netgrif.workflow.elastic.web.TaskSearchRequest;
import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface IElasticTaskService {

    Map<String, Float> fullTextFields();

    @Async
    void index(Task task);

    void indexNow(Task task);

    Page<ElasticTask> search(TaskSearchRequest request, LoggedUser user, Pageable pageable);

    long count(TaskSearchRequest request, LoggedUser user);
}