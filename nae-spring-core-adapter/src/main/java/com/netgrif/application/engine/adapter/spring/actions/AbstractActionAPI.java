package com.netgrif.application.engine.adapter.spring.actions;

import com.netgrif.application.engine.objects.petrinet.domain.Transition;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.filter.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractActionAPI {

    public abstract void make(List<Field<?>> field, String behavior, Transition on);

    public abstract void change(Field<?> field, Case targetCase, Task optionalTargetTask, String attribute, Object newValue);

    public abstract Page<Case> findCases(Predicate predicate, Pageable pageable);

    public abstract Page<Case> findCases(List<String> elasticStringQueries, String loggedUserId, Pageable pageable, Locale locale, boolean isIntersection);

    public abstract Case createCase(String identifier, String title, String color, String userId, Locale locale, Map<String, String> params);

    public abstract Page<Task> findTasks(Predicate predicate, Pageable pageable);

    public abstract Page<Task> findTasks(List<String> elasticStringQueries, String loggedUserId, Pageable pageable, Locale locale, boolean isIntersection);

    public abstract Task assignTask(String taskId, String userId, Map<String, String> params);

    public abstract Task cancelTask(String taskId, String userId, Map<String, String> params);

    public abstract Task finishTask(String taskId, String finishedById, Map<String, String> params);

    public abstract void assignRole(String roleId, String userId, Map<String, String> params);

    public abstract void removeRole(String roleId, String userId, Map<String, String> params);

    public abstract SetDataEventOutcome setData(String taskId, Map<String, Map<String, String>> dataSet, Map<String, String> params);

    public abstract Map<String, Field<?>> getData(String taskId, Map<String, String> params);


}
