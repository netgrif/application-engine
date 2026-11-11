package com.netgrif.application.engine.menu.services;


import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.menu.services.interfaces.DashboardItemService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
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

import java.util.*;

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
        itemCase = setDataWithExecute(itemCase, DashboardItemConstants.TASK_CONFIGURE, splitToPhases(outcome.getDataSet()));
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
        itemCase = setDataWithExecute(itemCase, DashboardItemConstants.TASK_CONFIGURE, splitToPhases(outcome.getDataSet()));
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

    /**
     * Assigns the task, sets the data one batch after another (in the given order) and finishes the task.
     * If a step fails, the values the batches touched are restored in the still assigned task, in the same
     * batch order, and the task is cancelled. An existing item thus keeps its data and its active state
     * (assigning the task deactivates it, cancelling does not bring it back).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Case setDataWithExecute(Case useCase, String transId, List<Map<String, Map<String, Object>>> dataSetBatches) throws TransitionNotExecutableException {
        AbstractUser loggedUser = userService.getLoggedOrSystem();
        String taskId = MenuItemUtils.findTaskIdInCase(useCase, transId);
        List<Map<String, Map<String, Object>>> rollbackBatches = buildRollbackBatches(useCase, dataSetBatches);
        Task task = taskService.findOne(taskId);
        task = taskService.assignTask(TaskParams.with()
                .task(task)
                .user(loggedUser)
                .build()).getTask();
        try {
            for (Map<String, Map<String, Object>> batch : dataSetBatches) {
                task = dataService.setData(task, ImportHelper.populateDataset((Map) batch)).getTask();
            }
            return taskService.finishTask(TaskParams.with()
                    .task(task)
                    .user(loggedUser)
                    .build()).getCase();
        } catch (RuntimeException | TransitionNotExecutableException e) {
            restoreQuietly(task, rollbackBatches);
            cancelQuietly(task, loggedUser);
            throw e;
        }
    }

    /**
     * Reads the current values of the fields the batches are going to write, in the same type and batch order.
     * Empty for an item that has no id yet: a case that is still being created has nothing to go back to.
     */
    private List<Map<String, Map<String, Object>>> buildRollbackBatches(Case useCase, List<Map<String, Map<String, Object>>> dataSetBatches) {
        Case current = workflowService.findOne(useCase.getStringId());
        Object currentId = current.getDataSet().get(DashboardItemConstants.FIELD_ID) == null ? null
                : current.getDataSet().get(DashboardItemConstants.FIELD_ID).getValue();
        if (currentId == null || currentId.toString().isEmpty()) {
            return List.of();
        }
        List<Map<String, Map<String, Object>>> rollback = new ArrayList<>();
        for (Map<String, Map<String, Object>> batch : dataSetBatches) {
            Map<String, Map<String, Object>> restore = new LinkedHashMap<>();
            batch.forEach((fieldId, entry) -> {
                Map<String, Object> restoreEntry = new LinkedHashMap<>();
                restoreEntry.put("type", entry.get("type"));
                restoreEntry.put("value", current.getDataSet().get(fieldId) == null ? null : current.getDataSet().get(fieldId).getValue());
                restore.put(fieldId, restoreEntry);
            });
            rollback.add(restore);
        }
        return rollback;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void restoreQuietly(Task task, List<Map<String, Map<String, Object>>> rollbackBatches) {
        try {
            Task assigned = taskService.findOne(task.getStringId());
            for (Map<String, Map<String, Object>> batch : rollbackBatches) {
                assigned = dataService.setData(assigned, ImportHelper.populateDataset((Map) batch)).getTask();
            }
        } catch (RuntimeException e) {
            log.warn("Could not restore the data of task {} after a failed dashboard item update", task.getStringId(), e);
        }
    }

    private void cancelQuietly(Task task, AbstractUser user) {
        try {
            taskService.cancelTask(TaskParams.with()
                    .task(taskService.findOne(task.getStringId()))
                    .user(user)
                    .build());
        } catch (RuntimeException e) {
            log.warn("Could not cancel task {} after a failed dashboard item update", task.getStringId(), e);
        }
    }

    /**
     * Splits the data set into the steps a user takes in the configuration form. Set events of the
     * dashboard_item process reset the icon and name whenever the target (is_internal, menu item), the
     * inherit flags or external_icon change, so the values of the last step must be written after them.
     * The order inside a phase matters too (is_internal clears external_url), hence ordered lists.
     */
    private List<Map<String, Map<String, Object>>> splitToPhases(Map<String, Map<String, Object>> dataSet) {
        List<List<String>> phases = List.of(
                List.of(DashboardItemConstants.FIELD_ID, DashboardItemConstants.FIELD_IS_INTERNAL,
                        DashboardItemConstants.FIELD_EXTERNAL_URL),
                List.of(DashboardItemConstants.FIELD_MENU_ITEM_LIST),
                List.of(DashboardItemConstants.FIELD_INHERIT_ICON, DashboardItemConstants.FIELD_INHERIT_NAME),
                List.of(DashboardItemConstants.FIELD_EXTERNAL_ICON));
        List<Map<String, Map<String, Object>>> batches = new ArrayList<>();
        Map<String, Map<String, Object>> rest = new LinkedHashMap<>(dataSet);
        for (List<String> phase : phases) {
            Map<String, Map<String, Object>> batch = new LinkedHashMap<>();
            phase.forEach(fieldId -> {
                if (rest.containsKey(fieldId)) {
                    batch.put(fieldId, rest.remove(fieldId));
                }
            });
            batches.add(batch);
        }
        batches.add(rest);
        return batches;
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
}
