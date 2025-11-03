package com.netgrif.application.engine.menu.services;


import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.menu.services.interfaces.IMenuItemService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.utils.MenuItemUtils;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.menu.FilterBody;
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemBody;
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemConstants;
import com.netgrif.application.engine.objects.workflow.domain.menu.ToDataSetOutcome;
import com.netgrif.application.engine.objects.workflow.domain.menu.configurations.ViewBody;
import com.netgrif.application.engine.objects.workflow.domain.menu.configurations.ViewConstants;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.runner.DefaultFiltersRunner;
import com.netgrif.application.engine.startup.runner.FilterRunner;
import com.netgrif.application.engine.startup.runner.MenuProcessRunner;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.params.DeleteCaseParams;
import com.netgrif.application.engine.workflow.params.TaskParams;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemService implements IMenuItemService {
    protected final IWorkflowService workflowService;
    protected final ITaskService taskService;
    protected final IDataService dataService;
    protected final UserService userService;
    protected final IElasticCaseService elasticCaseService;

    protected static final String DEFAULT_FOLDER_ICON = "folder";

    /**
     * Creates new filter case
     *
     * @param body filter data used for creation
     * @return initialized filter case instance with the provided data
     */
    @Override
    public Case createFilter(FilterBody body) throws TransitionNotExecutableException {
        AbstractUser loggedUser = userService.getLoggedOrSystem();
        Case filterCase = createCase(FilterRunner.FILTER_PETRI_NET_IDENTIFIER, body.getTitle().getDefaultValue(), ActorTransformer.toLoggedUser(loggedUser));
        filterCase.setIcon(body.getIcon());
        filterCase = workflowService.save(filterCase);
        ToDataSetOutcome dataSetOutcome = body.toDataSet();
        filterCase = setDataWithExecute(filterCase, DefaultFiltersRunner.AUTO_CREATE_TRANSITION, dataSetOutcome.getDataSet());
        log.trace("Created filter case [{}][{}]", filterCase.getStringId(), body.getTitle().getDefaultValue());
        return filterCase;
    }

    /**
     * Updates existing filter case
     *
     * @param filterCase filter to be updated
     * @param body       data values used for update
     * @return updated filter case instance
     */
    @Override
    public Case updateFilter(Case filterCase, FilterBody body) {
        filterCase.setIcon(body.getIcon());
        filterCase = workflowService.save(filterCase);
        ToDataSetOutcome dataSetOutcome = body.toDataSet();
        filterCase = setData(filterCase, DefaultFiltersRunner.DETAILS_TRANSITION, dataSetOutcome.getDataSet());
        log.trace("Updated filter case [{}][{}]", filterCase.getStringId(), body.getTitle().getDefaultValue());
        return filterCase;
    }


    /**
     * Creates menu item case and it's configuration cases
     *
     * @param body data used for creation
     * @return initialized menu item instance with the provided data
     * @throws IllegalArgumentException if the provided menu identifier already exists
     */
    @Override
    public Case createMenuItem(MenuItemBody body) throws TransitionNotExecutableException {
        log.debug("Creation of menu item case with identifier [{}] started.", body.getIdentifier());
        AbstractUser loggedUser = userService.getLoggedOrSystem();
        String sanitizedIdentifier = MenuItemUtils.sanitize(body.getIdentifier());

        if (existsMenuItem(sanitizedIdentifier)) {
            throw new IllegalArgumentException(String.format("Menu item identifier %s is not unique!", sanitizedIdentifier));
        }

        Case parentItemCase = getOrCreateFolderItem(body.getPath());

        I18nString newName = body.getMenuName();
        if (newName == null) {
            newName = new I18nString(body.getIdentifier());
        }
        Case menuItemCase = createCase(MenuProcessRunner.MENU_NET_IDENTIFIER, newName.getDefaultValue(),
                ActorTransformer.toLoggedUser(loggedUser));
        menuItemCase = workflowService.save(menuItemCase);

        parentItemCase = appendChildCaseIdAndSave(parentItemCase, menuItemCase.getStringId());

        Case viewCase = null;
        if (body.hasView()) {
            viewCase = createView(body.getView());
        }

        String nodePath = createNodePath(body.getPath(), sanitizedIdentifier);

        ToDataSetOutcome dataSetOutcome = body.toDataSet(parentItemCase.getStringId(), nodePath, viewCase);
        menuItemCase = setDataWithExecute(menuItemCase, MenuItemConstants.TRANS_INIT_ID, dataSetOutcome.getDataSet());
        log.debug("Created menu item case [{}] with identifier [{}].", menuItemCase.getStringId(), body.getIdentifier());
        return menuItemCase;
    }

    /**
     * Updates menu item case and it's configuration cases
     *
     * @param itemCase menu item case to be updated
     * @param body     data used for update
     * @return updated menu item case (configuration cases are updated, but not returned)
     */
    @Override
    public Case updateMenuItem(Case itemCase, MenuItemBody body) throws TransitionNotExecutableException {
        log.debug("Update of menu item case with identifier [{}] started.", body.getIdentifier());

        Case viewCase = findView(itemCase);
        viewCase = handleView(viewCase, body.getView());
        ToDataSetOutcome dataSetOutcome = body.toDataSet(viewCase);
        itemCase = setData(itemCase, MenuItemConstants.TRANS_SYNC_ID, dataSetOutcome.getDataSet());
        log.debug("Updated menu item case [{}] with identifier [{}].", itemCase.getStringId(), body.getIdentifier());
        return itemCase;
    }

    /**
     * Creates or updates menu item. At first menu item is searched by identifier. If found, then menu item will be
     * updated. If not, menu item will be created
     *
     * @param body data used for the update or creation
     * @return updated or created menu item case
     */
    @Override
    public Case createOrUpdateMenuItem(MenuItemBody body) throws TransitionNotExecutableException {
        Case itemCase = findMenuItem(MenuItemUtils.sanitize(body.getIdentifier()));
        if (itemCase != null) {
            return updateMenuItem(itemCase, body);
        } else {
            return createMenuItem(body);
        }
    }

    /**
     * Creates or ignore menu item. At first menu item is searched by identifier. If found, then nothing will happen.
     * If not, menu item will be created
     *
     * @param body data used for the creation
     * @return ignored or created menu item case
     */
    @Override
    public Case createOrIgnoreMenuItem(MenuItemBody body) throws TransitionNotExecutableException {
        Case itemCase = findMenuItem(body.getIdentifier());
        if (itemCase != null) {
            log.debug("Ignored creation or update of menu item case [{}] with identifier [{}].", itemCase.getStringId(),
                    body.getIdentifier());
            return itemCase;
        } else {
            return createMenuItem(body);
        }
    }

    /**
     * Finds menu item by identifier.
     *
     * @param identifier identifier of the menu item
     * @return Found menu item case. If not found, null will be returned
     */
    @Override
    public Case findMenuItem(String identifier) {
        return findMenuItem(identifier, false);
    }

    @Override
    public Case findMenuItem(String identifier, boolean retry) {
        return findCase(QCase.case$.processIdentifier
                .eq(MenuProcessRunner.MENU_NET_IDENTIFIER)
                .and(QCase.case$.dataSet.get(MenuItemConstants.FIELD_IDENTIFIER).value.eq(identifier)));
    }

    /**
     * Finds menu item by uri and name.
     *
     * @param path
     * @param name name of the menu item
     * @return Found menu item case. If not found, null will be returned
     */
    @Override
    public Case findMenuItem(String path, String name) {
        return findCase(QCase.case$.processIdentifier
                .eq(MenuProcessRunner.MENU_NET_IDENTIFIER)
                .and(QCase.case$.title.eq(name))
                .and(QCase.case$.dataSet.get(MenuItemConstants.FIELD_NODE_PATH).value.eq(path)));
    }

    /**
     * Finds folder case by UriNode
     *
     * @param path which folder represents
     * @return Found folder menu item case. If not found, null will be returned
     */
    @Override
    public Case findFolderCase(String path) {
//        TODO
        return findCase(QCase.case$.processIdentifier
                .eq(MenuProcessRunner.MENU_NET_IDENTIFIER)
                .and(QCase.case$.dataSet.get(MenuItemConstants.FIELD_NODE_PATH).value.eq(path)));
    }

    /**
     * Checks if the menu item exists
     *
     * @param identifier identifier of the menu item
     * @return true if the menu item exists
     */
    @Override
    public boolean existsMenuItem(String identifier) {
        //TODO mongo
        Case menuItem = this.workflowService.searchOne(QCase.case$.processIdentifier.eq("menu_item")
                .and(QCase.case$.dataSet.get(MenuItemConstants.FIELD_IDENTIFIER).value.eq(identifier)));
        return menuItem != null;
//        String query = String.format("processIdentifier:%s AND dataSet.%s.textValue.keyword:\"%s\"",
//                FilterRunner.MENU_NET_IDENTIFIER, MenuItemConstants.FIELD_IDENTIFIER, identifier);
//        return countCases(FilterRunner.MENU_NET_IDENTIFIER, query) > 0;
    }

    /**
     * Changes location of menu item. If non-existing location is provided, the new location is created and then the
     * item is moved. Cyclic destination path is forbidden (f.e. from <code>"/my_node"</code> to
     * <code>"/my_node/my_node2"</code>
     *
     * @param itemCase Instance of menu_item to be moved
     * @param destUri  destination path where the item will be moved. F.e. <code>"/my_new_node"</code>
     * @throws IllegalArgumentException if the path is forbidden
     */
    @Override
    public void moveItem(Case itemCase, String destUri) throws TransitionNotExecutableException {

        log.debug("Move of menu item case [{}] started. Destination path [{}]", itemCase.getStringId(), destUri);
        if (MenuItemUtils.isCyclicNodePath(itemCase, destUri)) {
            throw new IllegalArgumentException(String.format("Cyclic path not supported. Destination path: %s", destUri));
        }
        List<Case> casesToSave = new ArrayList<>();

        List<String> parentIdList = MenuItemUtils.getCaseIdsFromCaseRef(itemCase, MenuItemConstants.FIELD_PARENT_ID);
        if (parentIdList != null && !parentIdList.isEmpty()) {
            Case oldParent = removeChildItemFromParent(parentIdList.get(0), itemCase);
            casesToSave.add(oldParent);
        }

        Case newParent = getOrCreateFolderItem(destUri);
        if (newParent != null) {
            itemCase.getDataField(MenuItemConstants.FIELD_PARENT_ID).setValue(List.of(newParent.getStringId()));
            appendChildCaseIdInMemory(newParent, itemCase.getStringId());
            casesToSave.add(newParent);
        } else {
            itemCase.getDataField(MenuItemConstants.FIELD_PARENT_ID).setValue(null);
        }

        resolveAndHandleNewNodePath(itemCase, destUri);
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
        log.debug("Moved menu item case [{}]. Destination path was [{}]", itemCase.getStringId(), destUri);
    }

    /**
     * Duplicates menu item. It creates new menu_item instance with the same dataSet as the provided
     * item instance. The only difference is in title, menu_item_identifier and associations. Configuration cases are
     * duplicated as well.
     *
     * @param originItem    Menu item instance, which is duplicated
     * @param newTitle      Title of menu item, that is displayed in menu and tab. Cannot be empty or null.
     * @param newIdentifier unique menu item identifier
     * @return duplicated {@link Case} instance of menu_item
     * @throws IllegalArgumentException if the input data are invalid or the menu item of the new identifier already
     *                                  exists
     */
    @Override
    public Case duplicateItem(Case originItem, I18nString newTitle, String newIdentifier) throws TransitionNotExecutableException {
        log.debug("Duplication of menu item case [{}] started.", originItem.getStringId());
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
        Case duplicated = createCase(MenuProcessRunner.MENU_NET_IDENTIFIER, newTitle.getDefaultValue(),
                ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()));
        duplicated.setDataSet(originItem.getDataSet());
        duplicated.setTitle(newTitle.getDefaultValue());
        duplicated = workflowService.save(duplicated);

        String newNodePath = createNodePath(parentPath(String.valueOf(originItem.getDataSet().get(MenuItemConstants.FIELD_NODE_PATH))), sanitizedIdentifier);

        Map<String, Map<String, Object>> dataSet = new HashMap<>();
        dataSet.put(MenuItemConstants.FIELD_DUPLICATE_TITLE, Map.of("type", FieldType.I18N.getName(), "value",
                new I18nString("")));
        dataSet.put(MenuItemConstants.FIELD_DUPLICATE_IDENTIFIER, Map.of("type", FieldType.TEXT.getName(),
                "value", ""));
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
        log.debug("Duplicated menu item case [{}]. New title [{}] and new identifier [{}].", originItem.getStringId(),
                newTitle.getDefaultValue(), newIdentifier);
        return workflowService.findOne(duplicated.getStringId());
    }

    /**
     * Removes child menu item from the dataSet of the folder menu item case
     *
     * @param folderId  menu item identifier of the folder case
     * @param childItem menu item case of the child item to be removed
     * @return updated folder menu item case
     */
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

    protected Case findCase(Predicate predicate) {
        return workflowService.searchOne(predicate);
    }

    protected Case findCase(String processIdentifier, String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .process(Collections.singletonList(new CaseSearchRequest.PetriNet(processIdentifier)))
                .query(query)
                .build();
        Page<Case> resultPage = elasticCaseService.search(List.of(request), ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()),
                PageRequest.of(0, 1), Locale.getDefault(), false);

        return resultPage.hasContent() ? resultPage.getContent().get(0) : null;
    }

    protected long countCases(String processIdentifier, String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .process(Collections.singletonList(new CaseSearchRequest.PetriNet(processIdentifier)))
                .query(query)
                .build();
        return elasticCaseService.count(List.of(request), ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()),
                Locale.getDefault(), false);
    }

    protected Case duplicateView(Case viewCase) throws TransitionNotExecutableException {
        Case duplicatedAssociatedViewCase = null;
        if (MenuItemUtils.hasView(viewCase)) {
            String originViewId = MenuItemUtils.getCaseIdFromCaseRef(viewCase, ViewConstants.FIELD_VIEW_CONFIGURATION_ID);
            Case originViewCase = workflowService.findOne(originViewId);
            duplicatedAssociatedViewCase = duplicateView(originViewCase);
        }

        Case duplicatedViewCase = createCase(viewCase.getProcessIdentifier(), viewCase.getTitle(),
                ActorTransformer.toLoggedUser(userService.getLoggedOrSystem()));
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
        AbstractUser loggedUser = userService.getLoggedOrSystem();
        Case viewCase = createCase(body.getViewProcessIdentifier(), body.getViewProcessIdentifier(),
                ActorTransformer.toLoggedUser(loggedUser));

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
        viewCase = setDataWithExecute(viewCase, ViewConstants.TRANS_INIT_ID, dataSetOutcome.getDataSet());

        log.trace("Created configuration view case [{}] of identifier [{}]", viewCase.getStringId(),
                body.getViewProcessIdentifier());
        return viewCase;
    }

    protected Case updateView(Case viewCase, ViewBody body) throws TransitionNotExecutableException {
        Case filterCase = findFilter(viewCase);
        filterCase = handleFilter(filterCase, body.getFilterBody());

        Case associatedViewCase = findView(viewCase);
        associatedViewCase = handleView(associatedViewCase, body.getAssociatedViewBody());

        ToDataSetOutcome outcome = body.toDataSet(associatedViewCase, filterCase);
        viewCase = setData(viewCase, ViewConstants.TRANS_SYNC_ID, outcome.getDataSet());

        log.trace("Updated configuration view case [{}] of identifier [{}]", viewCase.getStringId(),
                body.getViewProcessIdentifier());
        return viewCase;
    }

    protected void removeView(Case viewCase) {
        workflowService.deleteCase(DeleteCaseParams.with()
                .useCase(viewCase)
                .build());
        log.trace("Removed configuration view case [{}].", viewCase.getStringId());
    }

    protected Case handleFilter(Case filterCase, FilterBody body) throws TransitionNotExecutableException {
        if (mustCreateFilter(filterCase, body)) {
            return createFilter(body);
        } else if (mustUpdateFilter(filterCase, body)) {
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
            resolveAndHandleNewNodePath(childCase, (String) parentFolder.getFieldValue(MenuItemConstants.FIELD_NODE_PATH));

            casesToSave.add(childCase);
            casesToSave.addAll(updateNodeInChildrenFoldersRecursive(childCase));
        }

        return casesToSave;
    }

    protected void resolveAndHandleNewNodePath(Case folderItem, String destUri) {
        String newNodePath = resolveNewNodePath(folderItem, destUri);
        folderItem.getDataField(MenuItemConstants.FIELD_NODE_PATH).setValue(newNodePath);
    }

    protected String resolveNewNodePath(Case folderItem, String destUri) {
        return destUri +
                MenuItemConstants.PATH_SEPARATOR +
                folderItem.getFieldValue(MenuItemConstants.FIELD_IDENTIFIER);
    }

    protected String createNodePath(String path, String identifier) {
        if (Objects.equals(path, MenuItemConstants.PATH_SEPARATOR)) {
            return path + identifier;
        } else {
            return path + MenuItemConstants.PATH_SEPARATOR + identifier;
        }
    }

    protected Case getOrCreateFolderItem(String path) throws TransitionNotExecutableException {
        String pathName = path.substring(path.lastIndexOf(MenuItemConstants.PATH_SEPARATOR) + 1);
        com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemBody body = new com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemBody(new I18nString(pathName), "folder");
        return getOrCreateFolderRecursive(path, body);
    }

    protected Case getOrCreateFolderRecursive(String path, MenuItemBody body) throws TransitionNotExecutableException {
        return getOrCreateFolderRecursive(path, body, null);
    }

    protected Case getOrCreateFolderRecursive(String path, MenuItemBody body, Case childFolderCase) throws TransitionNotExecutableException {
        AbstractUser loggedUser = userService.getLoggedOrSystem();
        Case folderCase = findFolderCase(path);
        if (folderCase != null) {
            if (childFolderCase != null) {
                folderCase = appendChildCaseIdAndSave(folderCase, childFolderCase.getStringId());
            }
            return folderCase;
        }

        folderCase = createCase(MenuProcessRunner.MENU_NET_IDENTIFIER, body.getMenuName().getDefaultValue(),
                ActorTransformer.toLoggedUser(loggedUser));

        ToDataSetOutcome dataSetOutcome = body.toDataSet(null, path, null);
        if (childFolderCase != null) {
            appendChildCaseIdInDataSet(folderCase, childFolderCase.getStringId(), dataSetOutcome.getDataSet());
            initializeParentId(childFolderCase, folderCase.getStringId());
        }

        if (hasParent(path)) {
            body = new com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemBody(new I18nString(nameFromPath(path)), "folder");
            String parentPath = parentPath(path);
            Case parentFolderCase = getOrCreateFolderRecursive(parentPath, body, folderCase);
            dataSetOutcome.putDataSetEntry(MenuItemConstants.FIELD_PARENT_ID, FieldType.CASE_REF, List.of(parentFolderCase.getStringId()));

        }
        folderCase = setDataWithExecute(folderCase, MenuItemConstants.TRANS_INIT_ID, dataSetOutcome.getDataSet());

        log.trace("Created folder menu item [{}] with identifier [{}]", folderCase.getStringId(), body.getIdentifier());
        return folderCase;
    }

    private Case initializeParentId(Case childFolderCase, String parentFolderCaseId) {
        childFolderCase.getDataField(MenuItemConstants.FIELD_PARENT_ID).setValue(Collections.singletonList(parentFolderCaseId));
        return workflowService.save(childFolderCase);
    }

    protected void appendChildCaseIdInDataSet(Case folderCase, String childItemCaseId, Map<String, Map<String, Object>> dataSet) {
        List<String> childIds = MenuItemUtils.getCaseIdsFromCaseRef(folderCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        if (childIds == null || childIds.isEmpty()) {
            childIds = List.of(childItemCaseId);
        } else {
            childIds.add(childItemCaseId);
        }

        dataSet.put(MenuItemConstants.FIELD_CHILD_ITEM_IDS, Map.of("type", FieldType.CASE_REF.getName(),
                "value", childIds));
        dataSet.put(MenuItemConstants.FIELD_HAS_CHILDREN, Map.of("type", FieldType.BOOLEAN.getName(),
                "value", !childIds.isEmpty()));
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
        return workflowService.createCase(CreateCaseParams.with()
                .processIdentifier(identifier)
                .title(title)
                .color("")
                .author(loggedUser)
                .build()).getCase();
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

    protected String nameFromPath(String path) {
        if (path == null || path.equals(MenuItemConstants.PATH_SEPARATOR) || path.isEmpty()) {
            return "";
        }
        if (path.lastIndexOf(MenuItemConstants.PATH_SEPARATOR) == 0) {
            return path.replace(MenuItemConstants.PATH_SEPARATOR, "");
        }
        return path.substring(path.lastIndexOf(MenuItemConstants.PATH_SEPARATOR));
    }

    protected String parentPath(String path) {
        if (path == null || path.equals(MenuItemConstants.PATH_SEPARATOR) || path.isEmpty() || path.lastIndexOf(MenuItemConstants.PATH_SEPARATOR) == 0) {
            return MenuItemConstants.PATH_SEPARATOR;
        }
        return path.substring(0, path.lastIndexOf(MenuItemConstants.PATH_SEPARATOR));
    }

    protected boolean hasParent(String path) {
        return path != null && !path.equals(MenuItemConstants.PATH_SEPARATOR) && !path.isEmpty();
    }

}
