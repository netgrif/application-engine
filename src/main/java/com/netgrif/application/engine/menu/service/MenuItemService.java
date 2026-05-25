package com.netgrif.application.engine.menu.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.menu.domain.*;
import com.netgrif.application.engine.menu.domain.configurations.ViewBody;
import com.netgrif.application.engine.menu.domain.configurations.ViewConstants;
import com.netgrif.application.engine.menu.domain.templates.Template;
import com.netgrif.application.engine.menu.service.interfaces.IMenuItemService;
import com.netgrif.application.engine.menu.utils.MenuItemUtils;
import com.netgrif.application.engine.petrinet.domain.*;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemService implements IMenuItemService {
    protected final IWorkflowService workflowService;
    protected final ITaskService taskService;
    protected final IDataService dataService;
    protected final IUserService userService;
    protected final IUriService uriService;
    protected final IElasticCaseService elasticCaseService;
    protected final MongoTemplate mongoTemplate;
    protected final IPetriNetService petriNetService;
    protected final IProcessRoleService processRoleService;

    protected Boolean existDatabaseIndexes;

    protected static final String DEFAULT_FOLDER_ICON = "folder";
    protected static final String GLOBAL_ROLE = "GLOBAL_ROLE";
    protected static final Map<String, String> CUSTOM_MENU_ITEM_INDEXES = Map.of(
            String.format("dataSet.%s.value", MenuItemConstants.FIELD_IDENTIFIER), MenuItemConstants.IDENTIFIER_INDEX_NAME,
            String.format("dataSet.%s.value", MenuItemConstants.FIELD_NODE_PATH), MenuItemConstants.NODE_PATH_INDEX_NAME
    );


    /**
     * Ensures custom MongoDB compound indexes for menu items are created on the Case collection.
     * Creates background indexes combining <code>processIdentifier</code> with menu item identifier
     * and node path fields to optimize menu item queries.
     *
     * @see #CUSTOM_MENU_ITEM_INDEXES
     */
    @Override
    public void ensureDatabaseIndexes() {
        log.info("Ensuring Mongo database menu item indexes");
        CUSTOM_MENU_ITEM_INDEXES.forEach( (indexKey, indexName) -> {
            org.bson.Document keys = new org.bson.Document()
                .append("processIdentifier", 1)
                .append(indexKey, 1);
            IndexDefinition index = new CompoundIndexDefinition(keys)
                    .named(indexName)
                    .background();
            mongoTemplate.indexOps(Case.class).ensureIndex(index);
        });
        existDatabaseIndexes = Boolean.TRUE;
    }

    /**
     * Creates menu item case and it's configuration cases
     *
     * @param body data used for creation
     *
     * @return initialized menu item instance with the provided data
     *
     * @throws IllegalArgumentException if the provided menu identifier already exists
     * */
    @Override
    public Case createMenuItem(MenuItemBody body) throws TransitionNotExecutableException {
        validateMenuItemBody(body);

        log.debug("Creation of menu item case with identifier [{}] started.", body.getIdentifier());
        IUser loggedUser = userService.getLoggedOrSystem();

        if (existsMenuItem(body.getIdentifier())) {
            throw new IllegalArgumentException(String.format("Menu item identifier %s is not unique!", body.getIdentifier()));
        }

        Case parentItemCase = getOrCreateFolderItem(body.getUri());
        I18nString newName = body.getMenuName();
        if (newName == null) {
            newName = new I18nString(body.getIdentifier());
        }
        Case menuItemCase = createCase(MenuItemConstants.PROCESS_IDENTIFIER, newName.getDefaultValue(),
                loggedUser.transformToLoggedUser());
        menuItemCase.setUriNodeId(uriService.findByUri(body.getUri()).getStringId());
        menuItemCase = workflowService.save(menuItemCase);

        parentItemCase = appendChildCaseIdAndSave(parentItemCase, menuItemCase.getStringId());

        String nodePath = createNodePath(body.getUri(), body.getIdentifier());
        uriService.getOrCreate(nodePath, UriContentType.CASE);

        Case viewCase = null;
        if (body.hasView()) {
            viewCase = createView(body.getView(), body.isUseTabbedView());
        }
        ToDataSetOutcome dataSetOutcome = body.toDataSet(parentItemCase.getStringId(), nodePath, viewCase);
        menuItemCase = setDataWithExecute(menuItemCase, MenuItemConstants.TRANS_SYS_INIT_ID, dataSetOutcome.getDataSet());
        log.debug("Created menu item case [{}] with identifier [{}].", menuItemCase.getStringId(), body.getIdentifier());
        return menuItemCase;
    }

    /**
     * Updates menu item case and it's configuration cases (recreates)
     *
     * @param itemCase menu item case to be updated
     * @param body data used for update
     *
     * @return recreated menu item case (configuration cases are recreated, but not returned)
     * */
    @Override
    public Case updateMenuItem(Case itemCase, MenuItemBody body) throws TransitionNotExecutableException {
        validateMenuItemBody(body);
        if (itemCase == null) {
            throw new IllegalArgumentException("Menu item case is null. Cannot update");
        }

        log.debug("Update of menu item case with identifier [{}] started.", body.getIdentifier());
        workflowService.deleteCase(itemCase);
        itemCase = createMenuItem(body);
        log.debug("Updated menu item case [{}] with identifier [{}].", itemCase.getStringId(), body.getIdentifier());
        return itemCase;
    }

    /**
     * Creates or updates menu item. At first menu item is searched by identifier. If found, then menu item will be
     * updated. If not, menu item will be created
     *
     * @param body data used for the update or creation
     *
     * @return updated or created menu item case
     * */
    @Override
    public Case createOrUpdateMenuItem(MenuItemBody body) throws TransitionNotExecutableException {
        if (body == null) {
            throw new IllegalArgumentException("Menu item body cannot be null");
        }
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
     *
     * @return ignored or created menu item case
     * */
    @Override
    public Case createOrIgnoreMenuItem(MenuItemBody body) throws TransitionNotExecutableException {
        if (body == null) {
            throw new IllegalArgumentException("Menu item body cannot be null");
        }
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
     *
     * @return Found menu item case. If not found, null will be returned
     * */
    @Override
    public Case findMenuItem(String identifier) {
        Query query = Query.query(
                Criteria.where("processIdentifier").is(MenuItemConstants.PROCESS_IDENTIFIER)
                        .and(String.format("dataSet.%s.value", MenuItemConstants.FIELD_IDENTIFIER)).is(identifier)
        );
        if (existDatabaseIndexes == null || !existDatabaseIndexes) {
            ensureDatabaseIndexes();
        }
        query.withHint(MenuItemConstants.IDENTIFIER_INDEX_NAME);
        List<Case> caseAsList = mongoTemplate.find(query, Case.class);
        Optional<Case> caseOptional = caseAsList.stream().findFirst();
        return caseOptional.map(aCase -> workflowService.findOne(aCase.getStringId())).orElse(null);
    }

    /**
     * Finds menu item by uri and name.
     *
     * @param uri string id of UriNode where the item exists
     * @param name name of the menu item
     *
     * @return Found menu item case. If not found, null will be returned
     * */
    @Override
    public Case findMenuItem(String uri, String name) {
        UriNode uriNode = uriService.findByUri(uri);
        String query = String.format("processIdentifier:%s AND title.keyword:\"%s\" AND uriNodeId:\"%s\"",
                MenuItemConstants.PROCESS_IDENTIFIER, name, uriNode.getStringId());
        return findCase(MenuItemConstants.PROCESS_IDENTIFIER, query);
    }

    /**
     * Finds folder case by UriNode
     *
     * @param node UriNode, which folder case represents
     *
     * @return Found folder menu item case. If not found, null will be returned
     * */
    @Override
    public Case findFolderCase(UriNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        Query query = Query.query(
                Criteria.where("processIdentifier").is(MenuItemConstants.PROCESS_IDENTIFIER)
                        .and(String.format("dataSet.%s.value", MenuItemConstants.FIELD_NODE_PATH)).is(node.getUriPath())
        );
        if (existDatabaseIndexes == null || !existDatabaseIndexes) {
            ensureDatabaseIndexes();
        }
        query.withHint(MenuItemConstants.NODE_PATH_INDEX_NAME);
        List<Case> caseAsList = mongoTemplate.find(query, Case.class);
        Optional<Case> caseOptional = caseAsList.stream().findFirst();
        return caseOptional.map(aCase -> workflowService.findOne(aCase.getStringId())).orElse(null);
    }

    /**
     * Checks if the menu item exists
     *
     * @param identifier identifier of the menu item
     *
     * @return true if the menu item exists
     * */
    @Override
    public boolean existsMenuItem(String identifier) {
        return findMenuItem(identifier) != null;
    }

    /**
     * Changes location of menu item. If non-existing location is provided, the new location is created and then the
     * item is moved. Cyclic destination path is forbidden (f.e. from <code>"/my_node"</code> to
     * <code>"/my_node/my_node2"</code>
     *
     * @param itemCase Instance of menu_item to be moved
     * @param destUri destination path where the item will be moved. F.e. <code>"/my_new_node"</code>
     *
     * @throws IllegalArgumentException if the path is forbidden
     * */
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
        log.debug("Moved menu item case [{}]. Destination path was [{}]", itemCase.getStringId(), destUri);
    }

    /**
     * Duplicates menu item. It creates new menu_item instance with the same dataSet as the provided
     * item instance. The only difference is in title, menu_item_identifier and associations. Configuration cases are
     * duplicated as well.
     *
     * @param originItem Menu item instance, which is duplicated
     * @param newTitle Title of menu item, that is displayed in menu and tab. Cannot be empty or null.
     * @param newIdentifier unique menu item identifier
     *
     * @return duplicated {@link Case} instance of menu_item
     *
     * @throws IllegalArgumentException if the input data are invalid or the menu item of the new identifier already
     * exists
     * */
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

        Case duplicated = createCase(MenuItemConstants.PROCESS_IDENTIFIER, newTitle.getDefaultValue(),
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

        setDataWithExecute(duplicated, MenuItemConstants.TRANS_SYS_INIT_ID, dataSet);

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
     * @param folderId menu item identifier of the folder case
     * @param childItem menu item case of the child item to be removed
     *
     * @return updated folder menu item case
     * */
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

    /**
     * Retrieves menu item data groups for the specified case and locale.
     *
     * @param caseId identifier of the menu item case
     * @param locale locale to use for retrieving localized data
     * @return list of data groups from the menu item case
     */
    @Override
    public List<DataGroup> getMenuItemData(String caseId, Locale locale) {
        Case menuItemCase = workflowService.findOne(caseId);
        String taskId = MenuItemUtils.findTaskIdInCase(menuItemCase, MenuItemConstants.TRANS_ALL_MENU_DATA);
        return dataService.getDataGroups(taskId, locale).getData();
    }

    /**
     * Handles the application of a configuration template to a menu item case.
     * <p>
     * This method retrieves the selected configuration template from the menu item case,
     * loads the corresponding template definition, and applies it by creating or updating
     * the associated view configuration. If no template is selected, the method returns
     * without making any changes.
     * </p>
     *
     * @param menuItemCase the menu item case to which the configuration template should be applied
     * @return a ConfigurationTemplateOutcome containing the dataSet outcome from applying the template, 
     *         or an empty outcome if no template was selected
     * @throws TransitionNotExecutableException if the workflow transition required for applying the template configuration cannot be executed
     * @throws IllegalArgumentException if the selected template identifier does not correspond to any registered template
     */
    @Override
    public ConfigurationTemplateOutcome handleConfigurationTemplate(Case menuItemCase) throws TransitionNotExecutableException {
        String selectedTemplate = (String) menuItemCase.getFieldValue(MenuItemConstants.FIELD_CONFIGURATION_TEMPLATES);
        if (selectedTemplate == null || selectedTemplate.isEmpty()) {
            return new ConfigurationTemplateOutcome();
        }

        String menuItemIdentifier = (String) menuItemCase.getFieldValue(MenuItemConstants.FIELD_IDENTIFIER);
        log.debug("Handling configuration template selection for menu item: [{}, {}] and configuration template: {}", 
                menuItemCase.getStringId(), menuItemIdentifier, selectedTemplate);
        Optional<Template> templateOpt = MenuItemTemplateHolder.get(selectedTemplate);
        if (templateOpt.isEmpty()) {
            throw new IllegalArgumentException(String.format("No configuration template found with name: %s", selectedTemplate));
        }

        MenuItemBody menuItemBody = templateOpt.get().getTemplate();
        Case viewCase = null;
        if (menuItemBody.hasView()) {
            viewCase = createView(menuItemBody.getView(), menuItemBody.isUseTabbedView());
        }
        ToDataSetOutcome dataSetOutcome = menuItemBody.toDataSetByConfigTemplate(viewCase);
        log.debug("For menu item: [{}. {}] was used configuration template: {}", menuItemCase.getStringId(),
                menuItemIdentifier, selectedTemplate);
        return new ConfigurationTemplateOutcome(dataSetOutcome);
    }

    @Override
    public Map<String, I18nString> collectRoles(List<ProcessRole> roles) {
        // todo rework authorization
        Map<String, I18nString> roleMap = new HashMap<>();
        for (ProcessRole role : roles) {
            String key;
            I18nString displayName;

            if (role.isGlobal()) {
                key = role.getImportId() + ":" + GLOBAL_ROLE;
                displayName = new I18nString(role.getName() + " (🌍 Global role)");
            } else {
                PetriNet net = petriNetService.get(new ObjectId(role.getNetId()));
                key = role.getImportId() + ":" + net.getIdentifier();
                displayName = new I18nString(role.getName() + " (" + net.getTitle() + ")");
            }

            roleMap.put(key, displayName);
        }
        return roleMap;
    }

    @Override
    public Map<String, I18nString> collectRoles(Map<String, String> roles) {
        // todo rework authorization
        Map<String, PetriNet> temp = new HashMap<>();
        Map<String, I18nString> result = new HashMap<>();

        for (Map.Entry<String, String> entry : roles.entrySet()) {
            if (GLOBAL_ROLE.equals(entry.getValue())) {
                Set<ProcessRole> findGlobalRole = processRoleService.findAllByImportId(ProcessRole.GLOBAL + entry.getKey());
                if (findGlobalRole == null || findGlobalRole.isEmpty()) {
                    continue;
                }
                Optional<ProcessRole> roleOpt = findGlobalRole.stream()
                        .filter(ProcessRole::isGlobal)
                        .findFirst();
                if (roleOpt.isEmpty()) {
                    continue;
                }
                result.put(roleOpt.get().getImportId() + ":" + GLOBAL_ROLE,
                        new I18nString(roleOpt.get().getName() + " (🌍 Global role)"));
            } else {
                if (!temp.containsKey(entry.getValue())) {
                    temp.put(entry.getValue(), petriNetService.getNewestVersionByIdentifier(entry.getValue()));
                }
                PetriNet net = temp.get(entry.getValue());
                Optional<ProcessRole> roleOpt = net.getRoles().values().stream()
                        .filter(r -> r.getImportId().equals(entry.getKey()))
                        .findFirst();
                roleOpt.ifPresent(processRole -> result.put(processRole.getImportId() + ":" + net.getIdentifier(),
                        new I18nString(processRole.getName() + " (" + net.getTitle() + ")")));
            }
        }

        return result;
    }

    protected void validateMenuItemBody(MenuItemBody body) {
        if (body == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }
        if (body.getIdentifier() == null) {
            throw new IllegalArgumentException("Identifier cannot be null");
        }
        if (body.getUri() == null || body.getUri().isBlank()) {
            throw new IllegalArgumentException("Uri cannot be null");
        } else {
            body.setUri(MenuItemUtils.sanitizeUriSegments(body.getUri(), uriService));
            List<String> uriSegments = List.of(body.getUri().split(uriService.getUriSeparator()));
            if (uriSegments.contains(body.getIdentifier())) {
                throw new IllegalArgumentException("Uri cannot contain this identifier");
            }
        }
    }

    protected Case findCase(String processIdentifier, String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .process(Collections.singletonList(new CaseSearchRequest.PetriNet(processIdentifier)))
                .query(query)
                .build();
        Page<Case> resultPage = elasticCaseService.search(List.of(request), userService.getLoggedOrSystem().transformToLoggedUser(),
                PageRequest.of(0, 1), Locale.getDefault(), false);

        return resultPage.hasContent() ? resultPage.getContent().get(0) : null;
    }

    protected long countCases(String processIdentifier, String query) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .process(Collections.singletonList(new CaseSearchRequest.PetriNet(processIdentifier)))
                .query(query)
                .build();
        return elasticCaseService.count(List.of(request), userService.getLoggedOrSystem().transformToLoggedUser(),
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
                userService.getLoggedOrSystem().transformToLoggedUser());
        duplicatedViewCase.setDataSet(viewCase.getDataSet());
        workflowService.save(duplicatedViewCase);

        Map<String, Map<String, Object>> dataSet = new HashMap<>();
        if (duplicatedAssociatedViewCase != null) {
            addConfigurationIntoDataSet(duplicatedAssociatedViewCase, dataSet);
        }

        return setDataWithExecute(duplicatedViewCase, MenuItemConstants.TRANS_SYS_INIT_ID, dataSet);
    }

    protected Case createView(ViewBody body, boolean isTabbed) throws TransitionNotExecutableException {
        IUser loggedUser = userService.getLoggedOrSystem();
        Case viewCase = createCase(body.getViewProcessIdentifier(), body.getViewProcessIdentifier(),
                loggedUser.transformToLoggedUser(), isTabbed);

        Case associatedViewCase = null;
        if (body.hasAssociatedView()) {
            associatedViewCase = createView(body.getAssociatedViewBody(), isTabbed);
        }

        ToDataSetOutcome dataSetOutcome = body.toDataSet(associatedViewCase);
        viewCase = setDataWithExecute(viewCase, ViewConstants.TRANS_INIT_ID, dataSetOutcome.getDataSet());

        log.trace("Created configuration view case [{}] of identifier [{}]", viewCase.getStringId(),
                body.getViewProcessIdentifier());
        return viewCase;
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
        if (newNodePath.startsWith("//")) {
            newNodePath = newNodePath.replace("//", uriService.getUriSeparator());
        }
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

        folderCase = createCase(MenuItemConstants.PROCESS_IDENTIFIER, body.getMenuName().getDefaultValue(),
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
        folderCase = setDataWithExecute(folderCase, MenuItemConstants.TRANS_SYS_INIT_ID, dataSetOutcome.getDataSet());

        log.trace("Created folder menu item [{}] with identifier [{}]", folderCase.getStringId(), body.getIdentifier());
        return folderCase;
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
        return workflowService.createCaseByIdentifier(identifier, title, "", loggedUser).getCase();
    }

    protected Case createCase(String identifier, String title, LoggedUser loggedUser, boolean isTabbed) {
        return workflowService.createCaseByIdentifier(identifier, title, "", loggedUser,
                Map.of("is_tabbed", String.valueOf(isTabbed))).getCase();
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
