package com.netgrif.application.engine.menu.services;


import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.menu.services.interfaces.DashboardItemService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.common.ResourceNotFoundException;
import com.netgrif.application.engine.objects.common.ResourceNotFoundExceptionCode;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.utils.MenuItemUtils;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.menu.ToDataSetOutcome;
import com.netgrif.application.engine.objects.workflow.domain.menu.dashboard.DashboardItemBody;
import com.netgrif.application.engine.objects.workflow.domain.menu.dashboard.DashboardItemConstants;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.params.TaskParams;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardItemServiceImpl implements DashboardItemService {

    protected final IWorkflowService workflowService;
    protected final UserService userService;
    protected final ITaskService taskService;
    protected final IDataService dataService;
    protected final IPetriNetService petriNetService;
    protected final IElasticCaseService elasticCaseService;

    /**
     * Creates a new dashboard item case if it does not already exist.
     * If a case with the same ID is found, it is returned instead of creating a new one.
     *
     * @param body The {@link DashboardItemBody} containing data for the dashboard item.
     * @return The created or existing {@link Case} representing the dashboard item.
     * @throws TransitionNotExecutableException if the task transition is not executable.
     */
    @Override
    public Case getOrCreate(DashboardItemBody body) throws TransitionNotExecutableException {
        MenuItemUtils.sanitize(body.getId());
        Case itemCase;

        itemCase = findById(body.getId());
        if (itemCase != null) {
            log.info("Dashboard item with id:{} already exists", body.getId());
            return itemCase;
        }

        LoggedUser loggedUser = ActorTransformer.toLoggedUser(userService.getLoggedOrSystem());
        itemCase = workflowService.createCase(CreateCaseParams.with()
                .processIdentifier(DashboardItemConstants.PROCESS_IDENTIFIER)
                .title(body.getName().getDefaultValue())
                .color("")
                .author(loggedUser)
                .build()).getCase();
        ToDataSetOutcome outcome = body.toDataSet();
        itemCase = setDataWithExecute(itemCase, DashboardItemConstants.TASK_CONFIGURE, outcome.getDataSet());
        return itemCase;
    }


    /**
     * Updates an existing dashboard item case with new data.
     *
     * @param itemCase The existing {@link Case} to update.
     * @param body The {@link DashboardItemBody} containing updated data.
     * @return The updated {@link Case} representing the dashboard item.
     * @throws TransitionNotExecutableException if the task transition is not executable.
     */
    @Override
    public Case update(Case itemCase, DashboardItemBody body) throws TransitionNotExecutableException {
        MenuItemUtils.sanitize(body.getId());
        ToDataSetOutcome outcome = body.toDataSet();
        itemCase = setDataWithExecute(itemCase, DashboardItemConstants.TASK_CONFIGURE, outcome.getDataSet());
        return itemCase;
    }

    /**
     * Finds an existing dashboard item case by its identifier.
     *
     * @param identifier The unique identifier of the dashboard item.
     * @return The {@link Case} representing the dashboard item, or null if not found.
     */
    @Override
    public Case findById(String identifier) {
        String query = String.format("processIdentifier:%s AND dataSet.%s.textValue.keyword:\"%s\"",
                DashboardItemConstants.PROCESS_IDENTIFIER, DashboardItemConstants.FIELD_ID, identifier);
        return findCase(DashboardItemConstants.PROCESS_IDENTIFIER, query);
    }

    protected Case setData(Case useCase, String transId, Map<String, Map<String, Object>> dataSet) {
        String taskId = MenuItemUtils.findTaskIdInCase(useCase, transId);
        return dataService.setData(taskId, ImportHelper.populateDataset((Map) dataSet)).getCase();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Case setDataWithExecute(Case useCase, String transId, Map<String, Map<String, Object>> dataSet) throws TransitionNotExecutableException {
        AbstractUser loggedUser = userService.getLoggedOrSystem();
        String taskId = MenuItemUtils.findTaskIdInCase(useCase, transId);
        Task task = taskService.findOne(taskId);
        task = taskService.assignTask(TaskParams.with()
                .task(task)
                .user(loggedUser)
                .build()).getTask();
        task = dataService.setData(task, ImportHelper.populateDataset((Map) dataSet)).getTask();
        return taskService.finishTask(TaskParams.with()
                .task(task)
                .user(loggedUser)
                .build()).getCase();
    }

    protected Case findCase(String processIdentifier, String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .process(Collections.singletonList(new CaseSearchRequest.PetriNet(processIdentifier)))
                .query(query)
                .build();
        Page<Case> resultPage = elasticCaseService.search(java.util.List.of(request),
                PageRequest.of(0, 1), Locale.getDefault(), false);

        return resultPage.hasContent() ? resultPage.getContent().get(0) : null;
    }
}
