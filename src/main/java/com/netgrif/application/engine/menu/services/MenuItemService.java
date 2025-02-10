package com.netgrif.application.engine.menu.services;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.MenuItemConstants;
import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.menu.domain.configurations.ViewBody;
import com.netgrif.application.engine.menu.domain.configurations.ViewConstants;
import com.netgrif.application.engine.menu.services.interfaces.IMenuItemService;
import com.netgrif.application.engine.menu.utils.MenuItemUtils;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.UriContentType;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.startup.DefaultFiltersRunner;
import com.netgrif.application.engine.startup.FilterRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemService implements IMenuItemService {
    // todo javadoc
    protected final IWorkflowService workflowService;
    protected final ITaskService taskService;
    protected final IDataService dataService;
    protected final IUserService userService;
    protected final IUriService uriService;

    protected static final String DEFAULT_FOLDER_ICON = "folder";

    @Override
    public Case createFilter(FilterBody body) throws TransitionNotExecutableException {
        IUser loggedUser = userService.getLoggedOrSystem();
        Case filterCase = createCase(FilterRunner.FILTER_PETRI_NET_IDENTIFIER, body.getTitle().getDefaultValue(), loggedUser.transformToLoggedUser());
        filterCase.setIcon(body.getIcon());
        filterCase = workflowService.save(filterCase);
        ToDataSetOutcome dataSetOutcome = body.toDataSet();
        return setDataWithExecute(filterCase, DefaultFiltersRunner.AUTO_CREATE_TRANSITION, dataSetOutcome.getDataSet());
    }

    @Override
    public Case updateFilter(Case filterCase, FilterBody body) {
        filterCase.setIcon(body.getIcon());
        filterCase = workflowService.save(filterCase);
        ToDataSetOutcome dataSetOutcome = body.toDataSet();
        return setData(filterCase, DefaultFiltersRunner.DETAILS_TRANSITION, dataSetOutcome.getDataSet());
    }

    @Override
    public Case createMenuItem(MenuItemBody body) throws TransitionNotExecutableException {
        IUser loggedUser = userService.getLoggedOrSystem();
        String sanitizedIdentifier = MenuItemUtils.sanitize(body.getIdentifier());

        if (existsMenuItem(sanitizedIdentifier)) {
            throw new IllegalArgumentException(String.format("Menu item identifier %s is not unique!", sanitizedIdentifier));
        }

        Case parentItemCase = getOrCreateFolderItem(body.getUri());
        I18nString newName = body.getMenuName();
        if (newName == null) {
            newName = new I18nString(body.getIdentifier());
        }
        Case menuItemCase = createCase(FilterRunner.MENU_NET_IDENTIFIER, newName.getDefaultValue(),
                loggedUser.transformToLoggedUser());
        menuItemCase.setUriNodeId(uriService.findByUri(body.getUri()).getStringId());
        menuItemCase = workflowService.save(menuItemCase);

        parentItemCase = appendChildCaseIdAndSave(parentItemCase, menuItemCase.getStringId());

        String nodePath = createNodePath(body.getUri(), sanitizedIdentifier);
        uriService.getOrCreate(nodePath, UriContentType.CASE);

        Case viewCase = null;
        if (body.hasView()) {
            viewCase = createView(body.getView());
        }
        ToDataSetOutcome dataSetOutcome = body.toDataSet(parentItemCase.getStringId(), nodePath, viewCase);
        return setDataWithExecute(menuItemCase, MenuItemConstants.TRANS_INIT_ID, dataSetOutcome.getDataSet());
    }

    @Override
    public Case updateMenuItem(Case itemCase, MenuItemBody body) throws TransitionNotExecutableException {
        String actualUriNodeId = uriService.findByUri(body.getUri()).getStringId();
        if (!itemCase.getUriNodeId().equals(actualUriNodeId)) {
            itemCase.setUriNodeId(actualUriNodeId);
            itemCase = workflowService.save(itemCase);
        }

        Case viewCase = findView(itemCase);
        viewCase = handleView(viewCase, body.getView());
        ToDataSetOutcome dataSetOutcome = body.toDataSet(viewCase);
        return setData(itemCase, MenuItemConstants.TRANS_SYNC_ID, dataSetOutcome.getDataSet());
    }

    @Override
    public Case createOrUpdateMenuItem(MenuItemBody body) throws TransitionNotExecutableException {
        Case itemCase = findMenuItem(MenuItemUtils.sanitize(body.getIdentifier()));
        if (itemCase != null) {
            return updateMenuItem(itemCase, body);
        } else {
            return createMenuItem(body);
        }
    }

    @Override
    public Case createOrIgnoreMenuItem(MenuItemBody body) throws TransitionNotExecutableException {
        Case itemCase = findMenuItem(body.getIdentifier());
        if (itemCase != null) {
            return itemCase;
        } else {
            return createMenuItem(body);
        }
    }

    @Override
    public Case findMenuItem(String identifier) {
        //        return findCaseElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.menu_item_identifier.textValue.keyword:\"$menuItemIdentifier\"" as String)
        Predicate predicate = QCase.case$.processIdentifier.eq(FilterRunner.MENU_NET_IDENTIFIER)
                .and(QCase.case$.dataSet.get("menu_item_identifier").value.eq(identifier));
        return workflowService.searchOne(predicate);
    }

    @Override
    public Case findMenuItem(String uri, String name) {
        UriNode uriNode = uriService.findByUri(uri);
//        return findCaseElastic("processIdentifier:\"$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER\" AND title.keyword:\"$name\" AND uriNodeId:\"$uriNode.stringId\"")
        Predicate predicate = QCase.case$.processIdentifier.eq(FilterRunner.MENU_NET_IDENTIFIER)
                .and(QCase.case$.title.eq(name))
                .and(QCase.case$.uriNodeId.eq(uriNode.getStringId()));
        return workflowService.searchOne(predicate);
    }

    @Override
    public Case findFolderCase(UriNode node) {
        // todo elastic problem
//        return findCaseElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.nodePath.textValue.keyword:\"$node.uriPath\"")
        Predicate predicate = QCase.case$.processIdentifier.eq(FilterRunner.MENU_NET_IDENTIFIER)
                .and(QCase.case$.dataSet.get("nodePath").value.eq(node.getUriPath()));
        return workflowService.searchOne(predicate);
    }

    @Override
    public boolean existsMenuItem(String identifier) {
        //        return countCasesElastic("processIdentifier:\"$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER\" AND dataSet.menu_item_identifier.fulltextValue.keyword:\"$menuItemIdentifier\"") > 0
        return findMenuItem(identifier) != null;
    }

    @Override
    public void moveItem(Case itemCase, String destUri) throws TransitionNotExecutableException {
        if (MenuItemUtils.isCyclicNodePath(itemCase, destUri)) {
            throw new IllegalArgumentException(String.format("Cyclic path not supported. Destination path: %s", destUri));
        }
        List<Case> casesToSave = new ArrayList<>();

        List<String> parentIdList = MenuItemUtils.getCaseIdsFromCaseRef(itemCase, MenuItemConstants.FIELD_PARENT_ID);
        if (parentIdList != null && !parentIdList.isEmpty()) {
            Case oldParent = removeChildItemFromParent(parentIdList.get(0), itemCase);
            casesToSave.add(oldParent);
        }

        UriNode destNode = uriService.getOrCreate(destUri, UriContentType.CASE);
        Case newParent = getOrCreateFolderItem(destNode.getUriPath());
        if (newParent != null) {
            itemCase.getDataField(MenuItemConstants.FIELD_PARENT_ID).setValue(List.of(newParent.getStringId()));
            appendChildCaseIdInMemory(newParent, itemCase.getStringId());
            casesToSave.add(newParent);
        } else {
            itemCase.getDataField(MenuItemConstants.FIELD_PARENT_ID).setValue(null);
        }

        itemCase.setUriNodeId(destNode.getStringId());
        resolveAndHandleNewNodePath(itemCase, destNode.getUriPath());
        casesToSave.add(itemCase);

        if (MenuItemUtils.hasFolderChildren(itemCase)) {
            List<Case> childrenToSave = updateNodeInChildrenFoldersRecursive(itemCase);
            casesToSave.addAll(childrenToSave);
        }

        for (Case useCase : casesToSave) {
            if (useCase != null) {
                workflowService.save(useCase);
            }
        }
    }

    @Override
    public Case duplicateItem(Case originItem, I18nString newTitle, String newIdentifier) throws TransitionNotExecutableException {
        if (newIdentifier == null || newIdentifier.isEmpty()) {
            throw new IllegalArgumentException("View item identifier is null or empty!");
        }
        if (newTitle == null || newTitle.getDefaultValue().isEmpty()) {
            throw new IllegalArgumentException("Default title is null or empty");
        }
        String sanitizedIdentifier = MenuItemUtils.sanitize(newIdentifier);
        if (existsMenuItem(sanitizedIdentifier)) {
            throw new IllegalArgumentException(String.format("View item identifier %s is not unique!", sanitizedIdentifier));
        }

        Case duplicatedViewCase = null;
        if (MenuItemUtils.hasView(originItem)) {
            String originViewId = MenuItemUtils.getCaseIdFromCaseRef(originItem, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
            Case originViewCase = workflowService.findOne(originViewId);
            duplicatedViewCase = duplicateView(originViewCase);
        }

        Case duplicated = createCase(FilterRunner.MENU_NET_IDENTIFIER, newTitle.getDefaultValue(),
                userService.getLoggedOrSystem().transformToLoggedUser());
        duplicated.setUriNodeId(originItem.getUriNodeId());
        duplicated.setDataSet(originItem.getDataSet());
        duplicated.setTitle(newTitle.getDefaultValue());
        duplicated = workflowService.save(duplicated);

        UriNode node = uriService.findById(originItem.getUriNodeId());
        String newNodePath = createNodePath(node.getUriPath(), sanitizedIdentifier);
        uriService.getOrCreate(newNodePath, UriContentType.CASE);

        Map<String, Map<String, Object>> dataSet = new HashMap<>();
        dataSet.put(MenuItemConstants.FIELD_DUPLICATE_TITLE, Map.of("type", FieldType.I18N.getName(), "value",
                new I18nString("")));
        dataSet.put(MenuItemConstants.FIELD_DUPLICATE_IDENTIFIER, Map.of("type", FieldType.TEXT.getName(),
                "value",""));
        dataSet.put(MenuItemConstants.FIELD_MENU_NAME, Map.of("type", FieldType.I18N.getName(),
                "value", newTitle));
        dataSet.put(MenuItemConstants.FIELD_TAB_NAME, Map.of("type", FieldType.I18N.getName(),
                "value", newTitle));
        dataSet.put(MenuItemConstants.FIELD_NODE_PATH, Map.of("type", FieldType.TEXT.getName(),
                "value", newNodePath));
        // Must be reset by button, because we have the same dataSet reference between originItem and duplicated
        dataSet.put(MenuItemConstants.FIELD_DUPLICATE_RESET_CHILD_ITEM_IDS, Map.of("type", FieldType.BUTTON.getName(),
                "value", 0));
        if (duplicatedViewCase != null) {
            addConfigurationIntoDataSet(duplicatedViewCase, dataSet);
        }

        setDataWithExecute(duplicated, MenuItemConstants.TRANS_INIT_ID, dataSet);

        List<String> parentIdAsList = MenuItemUtils.getCaseIdsFromCaseRef(originItem, MenuItemConstants.FIELD_PARENT_ID);
        if (parentIdAsList != null && !parentIdAsList.isEmpty()) {
            Case parent = workflowService.findOne(parentIdAsList.get(0));
            appendChildCaseIdAndSave(parent, duplicated.getStringId());
        }
        return workflowService.findOne(duplicated.getStringId());
    }

    @Override
    public Case removeChildItemFromParent(String folderId, Case childItem) {
        Case parentFolder = workflowService.findOne(folderId);
        List<String> childIds = MenuItemUtils.getCaseIdsFromCaseRef(parentFolder, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        if (childIds == null || childIds.isEmpty()) {
            return parentFolder;
        }
        childIds.remove(childItem.getStringId());
        parentFolder.getDataField(MenuItemConstants.FIELD_CHILD_ITEM_IDS).setValue(childIds);
        parentFolder.getDataField(MenuItemConstants.FIELD_HAS_CHILDREN).setValue(MenuItemUtils.hasFolderChildren(parentFolder));
        return workflowService.save(parentFolder);
    }

    protected Case duplicateView(Case viewCase) throws TransitionNotExecutableException {
        Case duplicatedAssociatedViewCase = null;
        if (MenuItemUtils.hasView(viewCase)) {
            String originViewId = MenuItemUtils.getCaseIdFromCaseRef(viewCase, ViewConstants.FIELD_VIEW_CONFIGURATION_ID);
            Case originViewCase = workflowService.findOne(originViewId);
            duplicatedAssociatedViewCase = duplicateView(originViewCase);
        }

        Case duplicatedViewCase = createCase(viewCase.getProcessIdentifier(), viewCase.getTitle(),
                userService.getLoggedOrSystem().transformToLoggedUser());
        duplicatedViewCase.setDataSet(viewCase.getDataSet());
        workflowService.save(duplicatedViewCase);

        Map<String, Map<String, Object>> dataSet = new HashMap<>();
        if (duplicatedAssociatedViewCase != null) {
            addConfigurationIntoDataSet(duplicatedAssociatedViewCase, dataSet);
        }

        return setDataWithExecute(duplicatedViewCase, MenuItemConstants.TRANS_INIT_ID, dataSet);
    }

    protected Case findView(Case itemOrViewCase) {
        return findCaseInCaseRef(itemOrViewCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
    }

    protected Case findFilter(Case viewCase) {
        return findCaseInCaseRef(viewCase, ViewConstants.FIELD_VIEW_FILTER_CASE);
    }

    protected Case findCaseInCaseRef(Case useCase, String caseRefId) {
        try {
            String caseId = MenuItemUtils.getCaseIdFromCaseRef(useCase, caseRefId);
            return workflowService.findOne(caseId);
        } catch (IllegalArgumentException | NullPointerException ignore) {
            return null;
        }
    }

    protected Case handleView(Case existingViewCase, ViewBody body) throws TransitionNotExecutableException {
        if (mustUpdateView(existingViewCase, body)) {
            return updateView(existingViewCase, body);
        } else if (mustCreateView(existingViewCase, body)) {
            return createView(body);
        } else if (mustRemoveView(existingViewCase, body)) {
            removeView(existingViewCase);
            return null;
        } else if (mustRemoveAndCreateView(existingViewCase, body)) {
            removeView(existingViewCase);
            return createView(body);
        } else {
            return null;
        }
    }

    protected Case createView(ViewBody body) throws TransitionNotExecutableException {
        IUser loggedUser = userService.getLoggedOrSystem();
        Case viewCase = createCase(body.getViewProcessIdentifier(), body.getViewProcessIdentifier(),
                loggedUser.transformToLoggedUser());

        Case associatedViewCase = null;
        if (body.hasAssociatedView()) {
            associatedViewCase = createView(body.getAssociatedViewBody());
        }
        Case filterCase = null;
        if (body.getFilterBody() != null) {
            if (body.getFilterBody().getFilter() != null) {
                filterCase = body.getFilterBody().getFilter();
            } else {
                filterCase = createFilter(body.getFilterBody());
            }
        }
        ToDataSetOutcome dataSetOutcome = body.toDataSet(associatedViewCase, filterCase);
        return setDataWithExecute(viewCase, ViewConstants.TRANS_INIT_ID, dataSetOutcome.getDataSet());
    }

    protected Case updateView(Case viewCase, ViewBody body) throws TransitionNotExecutableException {
        Case filterCase = findFilter(viewCase);
        filterCase = handleFilter(filterCase, body.getFilterBody());

        Case associatedViewCase = findView(viewCase);
        associatedViewCase = handleView(associatedViewCase, body.getAssociatedViewBody());

        ToDataSetOutcome outcome = body.toDataSet(associatedViewCase, filterCase);
        return setData(viewCase, ViewConstants.TRANS_SYNC_ID, outcome.getDataSet());
    }

    protected void removeView(Case viewCase) {
        workflowService.deleteCase(viewCase);
    }

    protected Case handleFilter(Case filterCase, FilterBody body) throws TransitionNotExecutableException {
        if (mustCreateFilter(filterCase, body)) {
            return createFilter(body);
        } else if (mustUpdateFilter(filterCase, body)){
            return updateFilter(filterCase, body);
        } else {
            return filterCase;
        }
    }

    protected boolean mustUpdateView(Case useCase, ViewBody body) {
        return body != null && useCase != null && useCase.getProcessIdentifier().equals(body.getViewProcessIdentifier());
    }

    protected boolean mustRemoveAndCreateView(Case useCase, ViewBody body) {
        return body != null && useCase != null && !useCase.getProcessIdentifier().equals(body.getViewProcessIdentifier());
    }

    protected boolean mustRemoveView(Case useCase, ViewBody body) {
        return body == null && useCase != null;
    }

    protected boolean mustCreateView(Case useCase, ViewBody body) {
        return body != null && useCase == null;
    }

    protected boolean mustCreateFilter(Case filterCase, FilterBody body) {
        return filterCase == null && body != null;
    }

    protected boolean mustUpdateFilter(Case filterCase, FilterBody body) {
        return filterCase != null && body != null;
    }

    protected List<Case> updateNodeInChildrenFoldersRecursive(Case parentFolder) {
        List<String> childItemIds = MenuItemUtils.getCaseIdsFromCaseRef(parentFolder, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        if (childItemIds == null || childItemIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Case> children = workflowService.findAllById(childItemIds);

        List<Case> casesToSave = new ArrayList<>();
        for (Case childCase : children) {
            UriNode parentNode = uriService.getOrCreate((String) parentFolder.getFieldValue(MenuItemConstants.FIELD_NODE_PATH),
                    UriContentType.CASE);
            childCase.setUriNodeId(parentNode.getStringId());
            resolveAndHandleNewNodePath(childCase, parentNode.getUriPath());

            casesToSave.add(childCase);
            casesToSave.addAll(updateNodeInChildrenFoldersRecursive(childCase));
        }

        return casesToSave;
    }

    protected void resolveAndHandleNewNodePath(Case folderItem, String destUri) {
        String newNodePath = resolveNewNodePath(folderItem, destUri);
        UriNode newNode = uriService.getOrCreate(newNodePath, UriContentType.CASE);
        folderItem.getDataField(MenuItemConstants.FIELD_NODE_PATH).setValue(newNode.getUriPath());
    }

    protected String resolveNewNodePath(Case folderItem, String destUri) {
        return destUri + uriService.getUriSeparator() + folderItem.getFieldValue(MenuItemConstants.FIELD_IDENTIFIER);
    }

    protected String createNodePath(String uri, String identifier) {
        if (Objects.equals(uri, uriService.getUriSeparator())) {
            return uri + identifier;
        } else {
            return uri + uriService.getUriSeparator() + identifier;
        }
    }

    protected Case getOrCreateFolderItem(String uri) throws TransitionNotExecutableException {
        UriNode node = uriService.getOrCreate(uri, UriContentType.CASE);
        MenuItemBody body = new MenuItemBody(new I18nString(node.getName()), DEFAULT_FOLDER_ICON);
        return getOrCreateFolderRecursive(node, body);
    }

    protected Case getOrCreateFolderRecursive(UriNode node, MenuItemBody body) throws TransitionNotExecutableException {
        return getOrCreateFolderRecursive(node, body, null);
    }
    protected Case getOrCreateFolderRecursive(UriNode node, MenuItemBody body, Case childFolderCase) throws TransitionNotExecutableException {
        IUser loggedUser = userService.getLoggedOrSystem();
        Case folderCase = findFolderCase(node);
        if (folderCase != null) {
            if (childFolderCase != null) {
                appendChildCaseIdAndSave(folderCase, childFolderCase.getStringId());
            }
            return folderCase;
        }

        folderCase = createCase(FilterRunner.MENU_NET_IDENTIFIER, body.getMenuName().getDefaultValue(),
                loggedUser.transformToLoggedUser());
        folderCase.setUriNodeId(node.getParentId());
        folderCase = workflowService.save(folderCase);

        ToDataSetOutcome dataSetOutcome = body.toDataSet(null, node.getUriPath(), null);
        if (childFolderCase != null) {
            appendChildCaseIdInDataSet(folderCase, childFolderCase.getStringId(), dataSetOutcome.getDataSet());
        }

        if (node.getParentId() != null) {
            UriNode parentNode = uriService.findById(node.getParentId());
            body = new MenuItemBody(new I18nString(parentNode.getName()), DEFAULT_FOLDER_ICON);

            Case parentFolderCase = getOrCreateFolderRecursive(parentNode, body, folderCase);
            dataSetOutcome.putDataSetEntry(MenuItemConstants.FIELD_PARENT_ID, FieldType.CASE_REF, List.of(parentFolderCase.getStringId()));
        }
        folderCase = setDataWithExecute(folderCase, MenuItemConstants.TRANS_INIT_ID, dataSetOutcome.getDataSet());

        return folderCase;
    }

    protected void appendChildCaseIdInDataSet(Case folderCase, String childItemCaseId, Map<String, Map<String, Object>> dataSet) {
        List<String> childIds = MenuItemUtils.getCaseIdsFromCaseRef(folderCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        if (childIds == null || childIds.isEmpty()) {
            dataSet.put(MenuItemConstants.FIELD_CHILD_ITEM_IDS, Map.of("type", FieldType.CASE_REF.getName(),
                    "value", List.of(childItemCaseId)));
        } else {
            childIds.add(childItemCaseId);
            dataSet.put(MenuItemConstants.FIELD_CHILD_ITEM_IDS, Map.of("type", FieldType.CASE_REF.getName(),
                    "value", childIds));
        }
        dataSet.put(MenuItemConstants.FIELD_HAS_CHILDREN, Map.of("type", FieldType.BOOLEAN.getName(),
                "value", MenuItemUtils.hasFolderChildren(folderCase)));
    }

    protected void appendChildCaseIdInMemory(Case folderCase, String childItemCaseId) {
        List<String> childIds = MenuItemUtils.getCaseIdsFromCaseRef(folderCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        if (childIds == null || childIds.isEmpty()) {
            folderCase.getDataField(MenuItemConstants.FIELD_CHILD_ITEM_IDS).setValue(List.of(childItemCaseId));
        } else {
            childIds.add(childItemCaseId);
            folderCase.getDataField(MenuItemConstants.FIELD_CHILD_ITEM_IDS).setValue(childIds);
        }
        folderCase.getDataField(MenuItemConstants.FIELD_HAS_CHILDREN).setValue(MenuItemUtils.hasFolderChildren(folderCase));
    }

    protected Case appendChildCaseIdAndSave(Case folderCase, String childItemCaseId) {
        Map<String, Map<String, Object>> dataSet = new HashMap<>();
        appendChildCaseIdInDataSet(folderCase, childItemCaseId, dataSet);
        return setData(folderCase, MenuItemConstants.TRANS_SYNC_ID, dataSet);
    }

    protected void addConfigurationIntoDataSet(Case configurationCase, Map<String, Map<String, Object>> dataSet) {
        dataSet.put(MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID, Map.of("type", FieldType.CASE_REF.getName(),
                "value", List.of(configurationCase.getStringId())));
        String taskId = MenuItemUtils.findTaskIdInCase(configurationCase, ViewConstants.TRANS_SETTINGS_ID);
        dataSet.put(MenuItemConstants.FIELD_VIEW_CONFIGURATION_FORM, Map.of("type", FieldType.TASK_REF.getName(),
                "value", List.of(taskId)));
    }

    protected Case createCase(String identifier, String title, LoggedUser loggedUser) {
        return workflowService.createCaseByIdentifier(identifier, title, "",loggedUser).getCase();
    }

    protected Case setData(Case useCase, String transId, Map<String, Map<String, Object>> dataSet) {
        String taskId = MenuItemUtils.findTaskIdInCase(useCase, transId);
        return setData(taskId, dataSet);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Case setData(String taskId, Map<String, Map<String, Object>> dataSet) {
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

}
