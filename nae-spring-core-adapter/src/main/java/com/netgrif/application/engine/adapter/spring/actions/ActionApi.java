package com.netgrif.application.engine.adapter.spring.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netgrif.application.engine.objects.auth.dto.AuthPrincipalDto;
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

import java.util.HashMap;
import java.util.List;

public interface ActionApi {

    GetDataEventOutcome getData(String taskId, HashMap<String, String> params);

    SetDataEventOutcome setData(String taskId, HashMap<String, HashMap<String, String>> dataSet, HashMap<String, String> params) throws JsonProcessingException;

    Page<Case> searchCases(String processIdentifier, Predicate predicate, Pageable pageable);

    Page<Case> searchCases(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Pageable pageable, Boolean isIntersection);

    CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, AuthPrincipalDto authPrincipalDto, HashMap<String, String> params);

    DeleteCaseEventOutcome deleteCase(String caseId, HashMap<String, String> params);

    Page<Task> searchTasks(String processIdentifier, Predicate predicate, Pageable pageable);

    Page<Task> searchTasks(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Pageable pageable, Boolean isIntersection);

    AssignTaskEventOutcome assignTask(String taskId, AuthPrincipalDto authPrincipalDto, HashMap<String, String> params) throws TransitionNotExecutableException;

    CancelTaskEventOutcome cancelTask(String taskId, AuthPrincipalDto authPrincipalDto, HashMap<String, String> params);

    FinishTaskEventOutcome finishTask(String taskId, AuthPrincipalDto authPrincipalDto, HashMap<String, String> params) throws TransitionNotExecutableException;
}
