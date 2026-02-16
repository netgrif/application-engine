package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.DelegateTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.ReloadTaskOutcome;
import com.netgrif.application.engine.workflow.params.DelegateTaskParams;
import com.netgrif.application.engine.workflow.params.TaskParams;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface ITaskService {

    ReloadTaskOutcome reloadTasks(Case useCase, boolean lazyCaseSave);

    Task findOne(String taskId);

    Page<Task> getAll(Pageable pageable, Locale locale);

    Page<Task> search(List<TaskSearchRequest> requests, Pageable pageable, Locale locale, Boolean isIntersection);

    long count(List<TaskSearchRequest> requests, Locale locale, Boolean isIntersection);

    Page<Task> findByCases(Pageable pageable, List<String> cases);

    Optional<Task> findOptionalById(String id);

    List<Task> findAllById(List<String> ids);

    Page<Task> findByUser(Pageable pageable, AbstractUser user);

    Task findById(String id);

    Page<Task> findByTransitions(Pageable pageable, List<String> transitions);

    Page<Task> searchAll(com.querydsl.core.types.Predicate predicate);

    Page<Task> search(com.querydsl.core.types.Predicate predicate, Pageable pageable);

    Task searchOne(com.querydsl.core.types.Predicate predicate);

    List<FinishTaskEventOutcome> finishTasks(List<Task> tasks, AbstractUser user) throws TransitionNotExecutableException;

    List<FinishTaskEventOutcome> finishTasks(List<Task> tasks, AbstractUser user, Map<String, String> params) throws TransitionNotExecutableException;

    FinishTaskEventOutcome finishTask(TaskParams taskParams) throws TransitionNotExecutableException;

    List<AssignTaskEventOutcome> assignTasks(List<Task> tasks, AbstractUser user) throws TransitionNotExecutableException;

    List<AssignTaskEventOutcome> assignTasks(List<Task> tasks, AbstractUser user, Map<String, String> params) throws TransitionNotExecutableException;

    AssignTaskEventOutcome assignTask(TaskParams taskParams) throws TransitionNotExecutableException;

    List<CancelTaskEventOutcome> cancelTasks(List<Task> tasks, AbstractUser user);

    List<CancelTaskEventOutcome> cancelTasks(List<Task> tasks, AbstractUser user, Map<String, String> params);

    CancelTaskEventOutcome cancelTask(TaskParams taskParams);

    @SuppressWarnings("unused")
    void cancelTasksWithoutReload(Set<String> transitions, String caseId);

    void cancelTasksWithoutReload(Set<String> transitions, String caseId, Map<String, String> params);

    DelegateTaskEventOutcome delegateTask(DelegateTaskParams delegateTaskParams) throws TransitionNotExecutableException;

    void resolveActorRef(Case useCase);

    Task resolveActorRef(Task task, Case useCase);

    void delete(List<Task> tasks, Case useCase);

    void delete(List<Task> tasks, String caseId);

    void delete(List<Task> tasks, String caseId, boolean force);

    void deleteTasksByCase(String caseId);

    void deleteTasksByCase(String caseId, boolean force);

    void deleteTasksByPetriNetId(String petriNetId);

    List<TaskReference> findAllByCase(String caseId, Locale locale);

    List<Task> findAllByCase(String caseId);

    Task save(Task task);

    List<Task> save(List<Task> tasks);

    SetDataEventOutcome getMainOutcome(Map<String, SetDataEventOutcome> outcomes, String taskId);
}
