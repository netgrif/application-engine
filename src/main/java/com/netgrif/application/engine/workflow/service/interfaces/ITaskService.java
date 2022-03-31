package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.DelegateTaskEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference;
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

    Page<Task> search(List<TaskSearchRequest> requests, Pageable pageable, LoggedUser user, Locale locale, Boolean isIntersection);

    long count(List<TaskSearchRequest> requests, LoggedUser user, Locale locale, Boolean isIntersection);

    Page<Task> findByCases(Pageable pageable, List<String> cases);

    List<Task> findAllById(List<String> ids);

    void createTasks(Case useCase);

    Page<Task> findByUser(Pageable pageable, IUser user);

    Task findById(String id);

    Page<Task> findByTransitions(Pageable pageable, List<String> transitions);

    Page<Task> searchAll(com.querydsl.core.types.Predicate predicate);

    Page<Task> search(com.querydsl.core.types.Predicate predicate, Pageable pageable);

    Task searchOne(com.querydsl.core.types.Predicate predicate);

    @Transactional(rollbackFor = Exception.class)
    List<FinishTaskEventOutcome> finishTasks(List<Task> tasks, IUser user) throws TransitionNotExecutableException;

    @Transactional
    FinishTaskEventOutcome finishTask(Task task, IUser user) throws TransitionNotExecutableException;

    FinishTaskEventOutcome finishTask(LoggedUser loggedUser, String taskId) throws IllegalArgumentException, TransitionNotExecutableException;

    FinishTaskEventOutcome finishTask(String taskId) throws IllegalArgumentException, TransitionNotExecutableException;

    @Transactional
    List<AssignTaskEventOutcome> assignTasks(List<Task> tasks, IUser user) throws TransitionNotExecutableException;

    @Transactional
    AssignTaskEventOutcome assignTask(Task task, IUser user) throws TransitionNotExecutableException;

    AssignTaskEventOutcome assignTask(LoggedUser loggedUser, String taskId) throws TransitionNotExecutableException;

    AssignTaskEventOutcome assignTask(String taskId) throws TransitionNotExecutableException;

    @Transactional(rollbackFor = Exception.class)
    List<CancelTaskEventOutcome> cancelTasks(List<Task> tasks, IUser user);

    @Transactional
    CancelTaskEventOutcome cancelTask(Task task, IUser user);

    CancelTaskEventOutcome cancelTask(LoggedUser loggedUser, String taskId);

    /**
     * cancel task action
     */
    @SuppressWarnings("unused")
    void cancelTasksWithoutReload(Set<String> transitions, String caseId);

    DelegateTaskEventOutcome delegateTask(LoggedUser loggedUser, String delegatedId, String taskId) throws TransitionNotExecutableException;

    void resolveUserRef(Case useCase);

    Task resolveUserRef(Task task, Case useCase);

    void delete(Iterable<? extends Task> tasks, Case useCase);

    void delete(Iterable<? extends Task> tasks, String caseId);

    void deleteTasksByCase(String caseId);

    void deleteTasksByPetriNetId(String petriNetId);

    List<TaskReference> findAllByCase(String caseId, Locale locale);

    Task save(Task task);

    SetDataEventOutcome getMainOutcome(Map<String, SetDataEventOutcome> outcomes, String taskId);
}