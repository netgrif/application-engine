package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.EventOutcome;
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface ITaskService {

    @Transactional
    void reloadTasks(Case useCase);

    Task findOne(String taskId);

    Page<Task> getAll(LoggedUser loggedUser, Pageable pageable, Locale locale);

    Page<Task> search(Map<String, Object> request, Pageable pageable, LoggedUser user);

    long count(Map<String, Object> request, LoggedUser user, Locale locale);

    Page<Task> findByCases(Pageable pageable, List<String> cases);

    void createTasks(Case useCase);

    Page<Task> findByUser(Pageable pageable, User user);

    Task findById(String id);

    Page<Task> findByTransitions(Pageable pageable, List<String> transitions);

    Page<Task> searchAll(com.querydsl.core.types.Predicate predicate);

    Task searchOne(com.querydsl.core.types.Predicate predicate);

    @Transactional(rollbackFor = Exception.class)
    void finishTasks(List<Task> tasks, User user) throws TransitionNotExecutableException;

    @Transactional
    EventOutcome finishTask(Task task, User user) throws TransitionNotExecutableException;

    EventOutcome finishTask(LoggedUser loggedUser, String taskId) throws IllegalArgumentException, TransitionNotExecutableException;

    EventOutcome finishTask(String taskId) throws IllegalArgumentException, TransitionNotExecutableException;

    @Transactional
    void assignTasks(List<Task> tasks, User user) throws TransitionNotExecutableException;

    @Transactional
    EventOutcome assignTask(Task task, User user) throws TransitionNotExecutableException;

    EventOutcome assignTask(LoggedUser loggedUser, String taskId) throws TransitionNotExecutableException;

    EventOutcome assignTask(String taskId) throws TransitionNotExecutableException;

    @Transactional(rollbackFor = Exception.class)
    void cancelTasks(List<Task> tasks, User user);

    @Transactional
    EventOutcome cancelTask(Task task, User user);

    EventOutcome cancelTask(LoggedUser loggedUser, String taskId);

    /**
     * cancel task action
     */
    @SuppressWarnings("unused")
    void cancelTasksWithoutReload(Set<String> transitions, String caseId);

    EventOutcome delegateTask(LoggedUser loggedUser, Long delegatedId, String taskId) throws TransitionNotExecutableException;

    void delete(Iterable<? extends Task> tasks, Case useCase);

    void delete(Iterable<? extends Task> tasks, String caseId);

    void deleteTasksByCase(String caseId);

    List<TaskReference> findAllByCase(String caseId, Locale locale);
}