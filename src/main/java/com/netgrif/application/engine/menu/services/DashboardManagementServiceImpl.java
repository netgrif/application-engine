package com.netgrif.application.engine.menu.services;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.menu.domain.dashboard.DashboardItemBody;
import com.netgrif.application.engine.menu.domain.dashboard.DashboardItemConstants;
import com.netgrif.application.engine.menu.domain.dashboard.DashboardManagementBody;
import com.netgrif.application.engine.menu.domain.dashboard.DashboardManagementConstants;
import com.netgrif.application.engine.menu.services.interfaces.DashboardManagementService;
import com.netgrif.application.engine.menu.utils.MenuItemUtils;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
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
    protected final IUserService userService;
    protected final ITaskService taskService;
    protected final IDataService dataService;
    protected final IPetriNetService petriNetService;
    protected final IElasticCaseService elasticCaseService;

    @Override
    public Case createDashboardManagement(DashboardManagementBody body) {
        Case managementCase;
        MenuItemUtils.sanitize(body.getId());

        managementCase = findDashboardManagement(body.getId());
        if (managementCase != null) {
            log.info("Dashboard management with id:{} already exists", body.getId());
            return managementCase;
        }
        addReferencedMenuItems(body);
        LoggedUser loggedUser = userService.getLoggedOrSystem().transformToLoggedUser();
        managementCase = workflowService.createCase(petriNetService.getNewestVersionByIdentifier(DashboardManagementConstants.PROCESS_IDENTIFIER).getStringId(), body.getName().getDefaultValue(), "", loggedUser).getCase();
        ToDataSetOutcome outcome = body.toDataSet();
        setData(managementCase, DashboardItemConstants.TASK_CONFIGURE, outcome.getDataSet());
        return managementCase;
    }

    @Override
    public Case createDashboardItem(DashboardItemBody body) throws TransitionNotExecutableException {
        MenuItemUtils.sanitize(body.getId());
        Case itemCase;

        itemCase = findDashboardItem(body.getId());
        if (itemCase != null) {
            log.info("Dashboard item with id:{} already exists", body.getId());
            return itemCase;
        }

        LoggedUser loggedUser = userService.getLoggedOrSystem().transformToLoggedUser();
        itemCase = workflowService.createCase(petriNetService.getNewestVersionByIdentifier(DashboardItemConstants.PROCESS_IDENTIFIER).getStringId(), body.getName().getDefaultValue(), "", loggedUser).getCase();
        ToDataSetOutcome outcome = body.toDataSet();
        itemCase = setDataWithExecute(itemCase, DashboardItemConstants.TASK_CONFIGURE, outcome.getDataSet());
        return itemCase;
    }

    @Override
    public Case updateDashboardManagement(Case managementCase, DashboardManagementBody body) {
        MenuItemUtils.sanitize(body.getId());
        addReferencedMenuItems(body);
        ToDataSetOutcome outcome = body.toDataSet();
        setData(managementCase, DashboardItemConstants.TASK_CONFIGURE, outcome.getDataSet());
        return managementCase;
    }


    @Override
    public Case updateDashboardItem(Case itemCase, DashboardItemBody body) throws TransitionNotExecutableException {
        MenuItemUtils.sanitize(body.getId());
        ToDataSetOutcome outcome = body.toDataSet();
        itemCase = setDataWithExecute(itemCase, DashboardItemConstants.TASK_CONFIGURE, outcome.getDataSet());
        return itemCase;
    }

    @Override
    public Case findDashboardItem(String identifier) {
        String query = String.format("processIdentifier:%s AND dataSet.%s.textValue.keyword:\"%s\"",
                DashboardItemConstants.PROCESS_IDENTIFIER, DashboardItemConstants.FIELD_ID, identifier);
        return findCase(DashboardItemConstants.PROCESS_IDENTIFIER, query);
    }

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
        IUser loggedUser = userService.getLoggedOrSystem();
        String taskId = MenuItemUtils.findTaskIdInCase(useCase, transId);
        Task task = taskService.findOne(taskId);
        task = taskService.assignTask(task, loggedUser).getTask();
        task = dataService.setData(task, ImportHelper.populateDataset((Map) dataSet)).getTask();
        return taskService.finishTask(task, loggedUser).getCase();
    }

    protected Case findCase(String processIdentifier, String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .process(Collections.singletonList(new CaseSearchRequest.PetriNet(processIdentifier)))
                .query(query)
                .build();
        Page<Case> resultPage = elasticCaseService.search(java.util.List.of(request), userService.getLoggedOrSystem().transformToLoggedUser(),
                PageRequest.of(0, 1), Locale.getDefault(), false);

        return resultPage.hasContent() ? resultPage.getContent().get(0) : null;
    }

    private void addReferencedMenuItems(DashboardManagementBody body) {
        if (body.getDashboardItems() != null && !body.getDashboardItems().isEmpty()) {
            HashMap<String, I18nString> menuItemToDashboardItem = new HashMap<>();
            body.getDashboardItems().keySet().forEach(dashboardItemId -> {
                Case dashboardItem = workflowService.findOne(dashboardItemId);
                menuItemToDashboardItem.put(dashboardItemId, new I18nString(String.valueOf(dashboardItem.getFieldValue(DashboardItemConstants.FIELD_MENU_ITEM_LIST))));
            });
            body.setMenuItemsToDashboardItems(menuItemToDashboardItem);
        }
    }
}
