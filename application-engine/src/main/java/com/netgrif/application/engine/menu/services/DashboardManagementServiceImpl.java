package com.netgrif.application.engine.menu.services;


import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.menu.services.interfaces.DashboardManagementService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.common.ResourceNotFoundException;
import com.netgrif.application.engine.objects.common.ResourceNotFoundExceptionCode;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.utils.MenuItemUtils;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.menu.ToDataSetOutcome;
import com.netgrif.application.engine.objects.workflow.domain.menu.dashboard.DashboardItemConstants;
import com.netgrif.application.engine.objects.workflow.domain.menu.dashboard.DashboardManagementBody;
import com.netgrif.application.engine.objects.workflow.domain.menu.dashboard.DashboardManagementConstants;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardManagementServiceImpl implements DashboardManagementService {

    protected final IWorkflowService workflowService;
    protected final UserService userService;
    protected final ITaskService taskService;
    protected final IDataService dataService;
    protected final IPetriNetService petriNetService;
    protected final IElasticCaseService elasticCaseService;


    /**
     * Creates a new dashboard management case if it does not already exist.
     * If a case with the same ID is found, it is returned instead of creating a new one.
     *
     * @param body The {@link DashboardManagementBody} containing data for the dashboard management.
     * @return The created or existing {@link Case} representing the dashboard management.
     */
    @Override
    public Case createDashboardManagement(DashboardManagementBody body) throws TransitionNotExecutableException {
        Case managementCase;

        managementCase = findDashboardManagement(MenuItemUtils.sanitize(body.getId()));
        if (managementCase != null) {
            log.info("Dashboard management with id:{} already exists", body.getId());
            return managementCase;
        }
        addReferencedMenuItems(body);
        LoggedUser loggedUser = ActorTransformer.toLoggedUser(userService.getLoggedOrSystem());
        managementCase = workflowService.createCase(CreateCaseParams.with()
                .processIdentifier(DashboardManagementConstants.PROCESS_IDENTIFIER)
                .title(body.getName().getDefaultValue())
                .color("")
                .author(loggedUser)
                .build()).getCase();
        ToDataSetOutcome outcome = body.toDataSet();
        managementCase = setDataWithExecute(managementCase, DashboardItemConstants.TASK_CONFIGURE, outcome.getDataSet());
        return managementCase;
    }

    /**
     * Updates an existing dashboard management case with new data.
     *
     * @param managementCase The existing {@link Case} to update.
     * @param body           The {@link DashboardManagementBody} containing updated data.
     * @return The updated {@link Case} representing the dashboard management.
     */
    @Override
    public Case updateDashboardManagement(Case managementCase, DashboardManagementBody body) throws TransitionNotExecutableException {
        addReferencedMenuItems(body);
        ToDataSetOutcome outcome = body.toDataSet();
        managementCase = setDataWithExecute(managementCase, DashboardItemConstants.TASK_CONFIGURE, outcome.getDataSet());
        return managementCase;
    }

    /**
     * Finds an existing dashboard management case by its identifier.
     *
     * @param identifier The unique identifier of the dashboard management.
     * @return The {@link Case} representing the dashboard management, or null if not found.
     */
    @Override
    public Case findDashboardManagement(String identifier) {
        String query = String.format("processIdentifier:%s AND dataSet.%s.textValue.keyword:\"%s\"",
                DashboardManagementConstants.PROCESS_IDENTIFIER, DashboardManagementConstants.FIELD_ID, identifier);
        return findCase(DashboardManagementConstants.PROCESS_IDENTIFIER, query);
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
        Page<Case> resultPage = elasticCaseService.search(java.util.List.of(request), ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()),
                PageRequest.of(0, 1), Locale.getDefault(), false);

        return resultPage.hasContent() ? resultPage.getContent().get(0) : null;
    }

    private void addReferencedMenuItems(DashboardManagementBody body) {
        if (body.getDashboardItems() == null || body.getDashboardItems().isEmpty()) {
            return;
        }
        HashMap<String, I18nString> menuItemToDashboardItem = new HashMap<>();
        body.getDashboardItems().keySet().forEach(dashboardItemId -> {
            Case dashboardItem = workflowService.findOne(dashboardItemId);
            menuItemToDashboardItem.put(dashboardItemId, new I18nString(String.valueOf(dashboardItem.getFieldValue(DashboardItemConstants.FIELD_MENU_ITEM_LIST))));
        });
        body.setMenuItemsToDashboardItems(menuItemToDashboardItem);
    }
}
