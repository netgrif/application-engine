package com.fmworkflow.workflow.service;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.Task;

import java.util.List;

public interface ITaskService {
    List<Task> findByCaseId(String caseId);

    void createTasks(Case useCase);

    List<Task> findByUser(User user);
}
