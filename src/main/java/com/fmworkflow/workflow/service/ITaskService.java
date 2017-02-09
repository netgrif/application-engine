package com.fmworkflow.workflow.service;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.petrinet.domain.throwable.TransitionNotStartableException;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.Task;

import java.util.List;

public interface ITaskService {
    List<Task> getAll();

    List<Task> findByCaseId(String caseId);

    void createTasks(Case useCase);

    List<Task> findByUser(User user);

    Task findById(Long id);

    void finishTask(String taskId);

    void takeTask(String taskId) throws TransitionNotStartableException;
}
