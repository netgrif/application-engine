package com.netgrif.workflow.elastic.service.interfaces;

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
    void index(ElasticTask task);

    void indexNow(ElasticTask task);

    Page<Task> search(TaskSearchRequest request, LoggedUser user, Pageable pageable);

    long count(TaskSearchRequest request, LoggedUser user);

    void remove(String taskId);
}