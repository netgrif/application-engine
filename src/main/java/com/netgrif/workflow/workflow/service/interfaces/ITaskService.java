package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.EventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface ITaskService {

    Task findOne(String taskId);

    Page<Task> getAll(LoggedUser loggedUser, Pageable pageable, Locale locale);

    Page<Task> search(Map<String, Object> request, Pageable pageable, LoggedUser user);

    Page<Task> findByCases(Pageable pageable, List<String> cases);

    void createTasks(Case useCase);

    Page<Task> findByUser(Pageable pageable, User user);

    Task findById(String id);

    Page<Task> findByTransitions(Pageable pageable, List<String> transitions);

    EventOutcome finishTask(LoggedUser loggedUser, String taskId) throws IllegalArgumentException, TransitionNotExecutableException;

    EventOutcome finishTask(String taskId) throws IllegalArgumentException, TransitionNotExecutableException;

    EventOutcome assignTask(LoggedUser loggedUser, String taskId) throws TransitionNotExecutableException;

    EventOutcome assignTask(String taskId) throws TransitionNotExecutableException;

    EventOutcome cancelTask(LoggedUser loggedUser, String taskId);

    EventOutcome delegateTask(LoggedUser loggedUser, String delegatedEmail, String taskId) throws TransitionNotExecutableException;

    void delete(Iterable<? extends Task> tasks, Case useCase);

    void delete(Iterable<? extends Task> tasks, String caseId);

    void deleteTasksByCase(String caseId);

    List<TaskReference> findAllByCase(String caseId, Locale locale);
}