package com.netgrif.workflow.elastic.service;

import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.scheduling.annotation.Async;

public interface IElasticTaskService {

    @Async
    void index(Task task);
}