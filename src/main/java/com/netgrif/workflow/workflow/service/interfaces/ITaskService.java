package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.DelegateTaskEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import com.netgrif.workflow.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface ITaskService {

    @Transactional
    void reloadTasks(Case useCase);

    Task findOne(String taskId);

    Page<Task> getAll(LoggedUser loggedUser, Pageable pageable, Locale locale);

    Page<Task> search(List<TaskSearchRequest> requests, Pageable pageable, LoggedUser user, Locale locale, Boolean isIntersection);

    long count(List<TaskSearchRequest> requests, LoggedUser user, Locale locale, Boolean isIntersection);

    Page<Task> findByCases(Pageable pageable, List<String> cases);

    List<Task> findAllById(List<String> ids);

    void createTasks(Case useCase);

    Page<Task> findByUser(Pageable pageable, User user);

    Task findById(String id);

    Page<Task> findByTransitions(Pageable pageable, List<String> transitions);

    Page<Task> searchAll(com.querydsl.core.types.Predicate predicate);

	Page<Task> search(com.querydsl.core.types.Predicate predicate, Pageable pageable);

	Task searchOne(com.querydsl.core.types.Predicate predicate);

    @Transactional(rollbackFor = Exception.class)
    FinishTaskEventOutcome finishTasks(List<Task> tasks, User user) throws TransitionNotExecutableException;

    @Transactional
    FinishTaskEventOutcome finishTask(Task task, User user) throws TransitionNotExecutableException;

    FinishTaskEventOutcome finishTask(LoggedUser loggedUser, String taskId) throws IllegalArgumentException, TransitionNotExecutableException;

    FinishTaskEventOutcome finishTask(String taskId) throws IllegalArgumentException, TransitionNotExecutableException;

    @Transactional
    AssignTaskEventOutcome assignTasks(List<Task> tasks, User user) throws TransitionNotExecutableException;

    @Transactional
    AssignTaskEventOutcome assignTask(Task task, User user) throws TransitionNotExecutableException;

    AssignTaskEventOutcome assignTask(LoggedUser loggedUser, String taskId) throws TransitionNotExecutableException;

    AssignTaskEventOutcome assignTask(String taskId) throws TransitionNotExecutableException;

    @Transactional(rollbackFor = Exception.class)
    CancelTaskEventOutcome cancelTasks(List<Task> tasks, User user);

    @Transactional
    CancelTaskEventOutcome cancelTask(Task task, User user);

    CancelTaskEventOutcome cancelTask(LoggedUser loggedUser, String taskId);

    /**
     * cancel task action
     */
    @SuppressWarnings("unused")
    void cancelTasksWithoutReload(Set<String> transitions, String caseId);

    DelegateTaskEventOutcome delegateTask(LoggedUser loggedUser, Long delegatedId, String taskId) throws TransitionNotExecutableException;

    void resolveUserRef(Case useCase);

    Task resolveUserRef(Task task, Case useCase);

    void delete(Iterable<? extends Task> tasks, Case useCase);

    void delete(Iterable<? extends Task> tasks, String caseId);

    void deleteTasksByCase(String caseId);

    List<TaskReference> findAllByCase(String caseId, Locale locale);

    Task save(Task task);
}