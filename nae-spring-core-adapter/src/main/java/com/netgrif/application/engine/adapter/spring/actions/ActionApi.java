package com.netgrif.application.engine.adapter.spring.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ActionApi {

    GetDataEventOutcome getData(String taskId, Map<String, String> params);

    SetDataEventOutcome setData(String taskId, Map<String, Map<String, String>> dataSet, Map<String, String> params) throws JsonProcessingException;

    Page<Case> searchCases(Predicate predicate, Pageable pageable);

    Page<Case> searchCases(List<String> elasticStringQueries, Pageable pageable, Boolean isIntersection);

    CreateCaseEventOutcome createCase(String netId, String title, String color, Map<String, String> params);

    CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, Map<String, String> params);

    DeleteCaseEventOutcome deleteCase(String caseId, Map<String, String> params);

    Page<Task> searchTasks(Predicate predicate, Pageable pageable);

    Page<Task> searchTasks(List<String> elasticStringQueries, Pageable pageable, Boolean isIntersection);

    AssignTaskEventOutcome assignTask(String taskId, String userId, String realmId, Map<String, String> params) throws TransitionNotExecutableException;

    CancelTaskEventOutcome cancelTask(String taskId, String userId, String realmId, Map<String, String> params);

    FinishTaskEventOutcome finishTask(String taskId, String userId, String realmId, Map<String, String> params) throws TransitionNotExecutableException;
}
