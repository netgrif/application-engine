package com.netgrif.application.engine.menu;

import com.mongodb.client.MongoCollection;
import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.menu.domain.*;
import com.netgrif.application.engine.menu.domain.configurations.*;
import com.netgrif.application.engine.menu.domain.templates.*;
import com.netgrif.application.engine.menu.service.MenuItemService;
import com.netgrif.application.engine.menu.service.MenuItemTemplateHolder;
import com.netgrif.application.engine.menu.utils.MenuItemUtils;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.domain.TaskPair;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class MenuItemServiceTest {
    
    @Autowired
    private TestHelper testHelper;
    
    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private IUriService uriService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private ITaskService taskService;

    @BeforeEach
    public void beforeEach() {
        testHelper.truncateDbs();
    }

    @Test
    public void ensureDatabaseIndexesTest() {
        mongoTemplate.getDb().drop();

        MongoCollection<Case> collection = mongoTemplate.getDb().getCollection("case", Case.class);
        long indexCountBefore = collection.listIndexes().into(new java.util.ArrayList<>()).size();
        assertEquals(0, indexCountBefore, "Expected no indexes after dropping database");

        menuItemService.ensureDatabaseIndexes();

        long indexCountAfter = collection.listIndexes().into(new java.util.ArrayList<>()).size();
        assertTrue(indexCountAfter > indexCountBefore, "Expected indexes to be created after calling ensureDatabaseIndexes");
    }

    @Test
    public void createMenuItemTest() throws TransitionNotExecutableException {
        assertThrows(IllegalArgumentException.class, () -> menuItemService.createMenuItem(null));
        MenuItemBody emptyBody = new MenuItemBody();
        assertThrows(IllegalArgumentException.class, () -> menuItemService.createMenuItem(emptyBody));
        emptyBody.setUri("/");
        assertThrows(IllegalArgumentException.class, () -> menuItemService.createMenuItem(emptyBody));
        emptyBody.setIdentifier("xxx");
        assertDoesNotThrow(() -> menuItemService.createMenuItem(emptyBody));
        emptyBody.setUri("/xxx2");
        emptyBody.setIdentifier("xxx3");
        assertDoesNotThrow(() -> menuItemService.createMenuItem(emptyBody));
        emptyBody.setUri("/xxx4");
        emptyBody.setIdentifier("xxx4");
        assertThrows(IllegalArgumentException.class, () -> menuItemService.createMenuItem(emptyBody));

        Map<String, ?> templateOptions = MenuItemTemplateHolder.transformToOptions();
        templateOptions.keySet().forEach((templateIdentifier) -> {
            Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(templateIdentifier,
                    uriService.getRoot().getUriPath(), templateIdentifier);
            assertTrue(menuItemBodyOpt.isPresent());
            try {
                createByTemplateAndAssert(menuItemBodyOpt.get());
            } catch (TransitionNotExecutableException e) {
                throw new RuntimeException(e);
            }
        });

        createAndAssertDetailed();
    }

    @SuppressWarnings("unchecked")
    private void createByTemplateAndAssert(MenuItemBody menuItemBody) throws TransitionNotExecutableException {
        caseRepository.deleteAll();
        Case menuItemCase = menuItemService.createMenuItem(menuItemBody);

        UriNode uriNode = uriService.findByUri(menuItemBody.getUri());
        assertEquals(uriNode.getStringId(), menuItemCase.getUriNodeId());

        assertEquals(menuItemBody.getIdentifier(), menuItemCase.getFieldValue(MenuItemConstants.FIELD_IDENTIFIER));
        I18nString actualMenuName = (I18nString) menuItemCase.getFieldValue(MenuItemConstants.FIELD_MENU_NAME);
        assertTrue(actualMenuName.equals(menuItemBody.getMenuName()) || actualMenuName.equals(new I18nString(menuItemBody.getIdentifier())));
        assertEquals(menuItemBody.isUseTabbedView(), menuItemCase.getFieldValue(MenuItemConstants.FIELD_USE_TABBED_VIEW));
        assertEquals(menuItemBody.isUseCustomView(), menuItemCase.getFieldValue(MenuItemConstants.FIELD_USE_CUSTOM_VIEW));
        assertEquals(menuItemBody.getConfigurationTemplateIdentifier(), menuItemCase.getFieldValue(MenuItemConstants.FIELD_CONFIGURATION_TEMPLATES));
        assertEquals((menuItemBody.getUri() + uriService.getUriSeparator() + menuItemBody.getIdentifier()).replaceAll("//", uriService.getUriSeparator()),
                menuItemCase.getFieldValue(MenuItemConstants.FIELD_NODE_PATH));

        if (menuItemBody.getView() == null) {
            assertEquals(1 + 1, caseRepository.count());

            List<String> viewConfigurationIdValue = (List<String>) menuItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
            assertTrue(viewConfigurationIdValue == null || viewConfigurationIdValue.isEmpty());

            List<String> viewConfigurationFormValue = (List<String>) menuItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_FORM);
            assertTrue(viewConfigurationFormValue == null || viewConfigurationFormValue.isEmpty());

            List<String> viewConfigurationAllDataFormValue = (List<String>) menuItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_ALL_DATA_FORM);
            assertTrue(viewConfigurationAllDataFormValue == null || viewConfigurationAllDataFormValue.isEmpty());
        } else {
            List<String> viewConfigurationIdValue = (List<String>) menuItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
            assertEquals(1, viewConfigurationIdValue.size());
            Case viewCase = workflowService.findOne(viewConfigurationIdValue.get(0));

            List<String> viewConfigurationFormValue = (List<String>) menuItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_FORM);
            assertEquals(1, viewConfigurationFormValue.size());
            assertTrue(viewConfigurationFormValue.contains(getTaskId(viewCase, ViewConstants.TRANS_SETTINGS_ID)));

            List<String> viewConfigurationAllDataFormValue = (List<String>) menuItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_ALL_DATA_FORM);
            assertEquals(1, viewConfigurationAllDataFormValue.size());
            assertTrue(viewConfigurationAllDataFormValue.contains(getTaskId(viewCase, ViewConstants.TRANS_ALL_MENU_DATA_ID)));

            if (!menuItemBody.getView().hasAssociatedView()) {
                assertEquals(2 + 1, caseRepository.count());
            } else {
                assertEquals(3 + 1, caseRepository.count());

                List<String> nextViewConfigurationIdValue = (List<String>) viewCase.getFieldValue(ViewConstants.FIELD_VIEW_CONFIGURATION_ID);
                assertEquals(1, nextViewConfigurationIdValue.size());
                Case nextViewCase = workflowService.findOne(nextViewConfigurationIdValue.get(0));

                List<String> nextViewConfigurationFormValue = (List<String>) viewCase.getFieldValue(ViewConstants.FIELD_VIEW_CONFIGURATION_FORM);
                assertEquals(1, nextViewConfigurationFormValue.size());
                assertTrue(nextViewConfigurationFormValue.contains(getTaskId(nextViewCase, ViewConstants.TRANS_SETTINGS_ID)));

                List<String> nextViewConfigurationAllDataFormValue = (List<String>) viewCase.getFieldValue(ViewConstants.FIELD_VIEW_CONFIGURATION_ALL_DATA_FORM);
                assertEquals(1, nextViewConfigurationAllDataFormValue.size());
                assertTrue(nextViewConfigurationAllDataFormValue.contains(getTaskId(nextViewCase, ViewConstants.TRANS_ALL_MENU_DATA_ID)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void createAndAssertDetailed() throws TransitionNotExecutableException {
        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(TabbedCaseViewTemplate.IDENTIFIER);
        assertTrue(menuItemBodyOpt.isPresent());
        MenuItemBody menuItemBody = menuItemBodyOpt.get();
        menuItemBody.setUri("/netgrif/test");
        menuItemBody.setIdentifier("new_menu_item");
        menuItemBody.setMenuIcon("device_hub");
        assertNotNull(menuItemBody.getView());
        assertNotNull(menuItemBody.getView().getFilterBody());
        menuItemBody.getView().getFilterBody().setQuery("processIdentifier:menu_item");
        menuItemBody.getView().getFilterBody().setType(FieldType.CASE_FILTER);
        ((CaseViewBody) menuItemBody.getView()).setDefaultHeaders(List.of("meta-title", "meta-processIdentifier"));
        assertNotNull(menuItemBody.getView().getAssociatedViewBody());
        ((TaskViewBody) menuItemBody.getView().getAssociatedViewBody()).setDefaultHeaders(List.of("meta-title"));
        menuItemBody.setAllowedRoles(menuItemService.collectRoles(Map.of("admin", "menu_item")));
        menuItemBody.setBannedRoles(menuItemService.collectRoles(Map.of("system", "menu_item")));

        Case menuItemCase = menuItemService.createMenuItem(menuItemBody);

        // MENU ITEM
        UriNode leafNode = uriService.findByUri("/netgrif/test/new_menu_item");
        assertNotNull(leafNode);
        assertEquals(menuItemCase.getUriNodeId(), uriService.findByUri("/netgrif/test").getStringId());
        assertEquals("device_hub", menuItemCase.getFieldValue(MenuItemConstants.FIELD_MENU_ICON));
        I18nString menuName = (I18nString) menuItemCase.getFieldValue(MenuItemConstants.FIELD_MENU_NAME);
        assertEquals(new I18nString("new_menu_item"), menuName);
        assertEquals("new_menu_item", menuItemCase.getFieldValue(MenuItemConstants.FIELD_IDENTIFIER).toString());

        Map<String, ?> bannedRolesOptions = menuItemCase.getDataSet().get(MenuItemConstants.FIELD_BANNED_ROLES).getOptions();
        assertTrue(bannedRolesOptions.containsKey("system:menu_item"));
        Map<String, ?> allowedRolesOptions = menuItemCase.getDataSet().get(MenuItemConstants.FIELD_ALLOWED_ROLES).getOptions();
        assertTrue(allowedRolesOptions.containsKey("admin:menu_item"));

        // CASE VIEW
        assertEquals(true, menuItemCase.getFieldValue(MenuItemConstants.FIELD_USE_TABBED_VIEW));
        assertEquals(MenuItemViewType.CASE_VIEW.getIdentifier(), menuItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE));

        String caseViewId = MenuItemUtils.getCaseIdFromCaseRef(menuItemCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertNotNull(caseViewId);
        Case caseView = workflowService.findOne(caseViewId);
        DataField filterDataField = caseView.getDataField(CaseViewConstants.FIELD_FILTER);
        assertEquals("processIdentifier:menu_item", filterDataField.getValue());
        assertEquals(true, caseView.getFieldValue(CaseViewConstants.FIELD_ALL_ALLOWED_NETS));
        assertEquals(true, caseView.getFieldValue(CaseViewConstants.FIELD_INHERIT_ALLOWED_NETS));

        List<String> caseDefaultHeaders = (List<String>) caseView.getFieldValue(CaseViewConstants.FIELD_DEFAULT_HEADERS);
        assertEquals(2, caseDefaultHeaders.size());
        assertTrue(caseDefaultHeaders.containsAll(List.of("meta-title", "meta-processIdentifier")));
        assertEquals(MenuItemViewType.TASK_VIEW.getIdentifier(), caseView.getFieldValue(CaseViewConstants.FIELD_CONFIGURATION_TYPE));

        // TASK VIEW
        String taskViewId = MenuItemUtils.getCaseIdFromCaseRef(caseView, CaseViewConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertNotNull(taskViewId);
        Case taskView = workflowService.findOne(taskViewId);
        List<String> taskDefaultHeaders = (List<String>) taskView.getFieldValue(TaskViewConstants.FIELD_DEFAULT_HEADERS);
        assertEquals(1, taskDefaultHeaders.size());
        assertTrue(taskDefaultHeaders.contains("meta-title"));

        // FOLDERS
        Case testFolder = findMenuItem("test");
        Case netgrifFolder = findMenuItem("netgrif");

        UriNode testNode = uriService.findByUri("/netgrif");
        UriNode netgrifNode = uriService.getRoot();

        Case rootFolder = findMenuItem("root");

        assertNotNull(testFolder);
        assertNotNull(testNode);
        assertEquals(testNode.getStringId(), testFolder.getUriNodeId());

        String testFolderParentId = MenuItemUtils.getCaseIdFromCaseRef(testFolder, MenuItemConstants.FIELD_PARENT_ID);
        assertEquals(netgrifFolder.getStringId(), testFolderParentId);

        List<String> testFolderChildIds = MenuItemUtils.getCaseIdsFromCaseRef(testFolder, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(testFolderChildIds);
        assertTrue(testFolderChildIds.contains(menuItemCase.getStringId()));

        String itemParentId = MenuItemUtils.getCaseIdFromCaseRef(menuItemCase, MenuItemConstants.FIELD_PARENT_ID);
        assertEquals(testFolder.getStringId(), itemParentId);

        assertEquals(netgrifNode.getStringId(), netgrifFolder.getUriNodeId());

        String netgrifFolderParentId = MenuItemUtils.getCaseIdFromCaseRef(netgrifFolder, MenuItemConstants.FIELD_PARENT_ID);
        assertEquals(rootFolder.getStringId(), netgrifFolderParentId);

        List<String> netgrifFolderChildIds = MenuItemUtils.getCaseIdsFromCaseRef(netgrifFolder, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(netgrifFolderChildIds);
        assertTrue(netgrifFolderChildIds.contains(testFolder.getStringId()));

        String rootFolderParentId = MenuItemUtils.getCaseIdFromCaseRef(rootFolder, MenuItemConstants.FIELD_PARENT_ID);
        assertNull(rootFolderParentId);

        List<String> rootFolderChildIds = MenuItemUtils.getCaseIdsFromCaseRef(rootFolder, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(rootFolderChildIds);
        assertTrue(rootFolderChildIds.contains(netgrifFolder.getStringId()));

        Optional<MenuItemBody> bodyOpt = MenuItemTemplateHolder.get("simple_case_view", "/netgrif/test/new_menu_item", "someIdentifier");
        assertTrue(bodyOpt.isPresent());
        // cannot place item inside non-folder item
        assertThrows(IllegalArgumentException.class, () -> menuItemService.createMenuItem(bodyOpt.get()));
    }

    @Test
    public void updateMenuItemTest() throws TransitionNotExecutableException {
        assertThrows(IllegalArgumentException.class, () -> menuItemService.updateMenuItem(null, null));

        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(SimpleTaskViewTemplate.IDENTIFIER);
        assertTrue(menuItemBodyOpt.isPresent());
        MenuItemBody menuItemBody = menuItemBodyOpt.get();
        menuItemBody.setUri(uriService.getRoot().getUriPath());
        menuItemBody.setIdentifier("test");
        Case menuItemCase = menuItemService.createMenuItem(menuItemBody);

        String oldMenuItemCaseId = menuItemCase.getStringId();
        String oldViewCaseId = MenuItemUtils.getCaseIdFromCaseRef(menuItemCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
        Case simpleTaskViewCase = workflowService.findOne(oldViewCaseId);
        assertEquals("task_view_configuration", simpleTaskViewCase.getProcessIdentifier());
        assertNull(MenuItemUtils.getCaseIdFromCaseRef(simpleTaskViewCase, ViewConstants.FIELD_VIEW_CONFIGURATION_ID));

        assertThrows(IllegalArgumentException.class, () -> menuItemService.updateMenuItem(menuItemCase, null));
        assertThrows(IllegalArgumentException.class, () -> menuItemService.updateMenuItem(menuItemCase, new MenuItemBody()));

        menuItemBodyOpt = MenuItemTemplateHolder.get(TabbedCaseViewTemplate.IDENTIFIER, uriService.getRoot().getUriPath(),
                "test");
        assertTrue(menuItemBodyOpt.isPresent());

        Case updatedMenuItemCase = menuItemService.updateMenuItem(menuItemCase, menuItemBodyOpt.get());

        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(oldMenuItemCaseId));
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(oldViewCaseId));

        String newMenuItemCaseId = updatedMenuItemCase.getStringId();
        assertNotEquals(oldMenuItemCaseId, newMenuItemCaseId);
        String newViewCaseId = MenuItemUtils.getCaseIdFromCaseRef(updatedMenuItemCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertNotEquals(oldViewCaseId, newViewCaseId);
        Case caseViewCase = workflowService.findOne(newViewCaseId);
        assertEquals("case_view_configuration", caseViewCase.getProcessIdentifier());
        String taskViewCaseId = MenuItemUtils.getCaseIdFromCaseRef(caseViewCase, ViewConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertNotNull(taskViewCaseId);
        Case taskViewCase = workflowService.findOne(taskViewCaseId);
        assertEquals("task_view_configuration", taskViewCase.getProcessIdentifier());
    }

    @Test
    public void createOrUpdateMenuItemTest() throws TransitionNotExecutableException {
        assertThrows(IllegalArgumentException.class, () -> menuItemService.createOrUpdateMenuItem(null));

        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(CustomViewTemplate.IDENTIFIER,
                uriService.getRoot().getUriPath(), "test");
        assertTrue(menuItemBodyOpt.isPresent());

        Case firstMenuItemCase = menuItemService.createOrUpdateMenuItem(menuItemBodyOpt.get());
        Case secondMenuItemCase = menuItemService.createOrUpdateMenuItem(menuItemBodyOpt.get());

        assertNotEquals(firstMenuItemCase.getStringId(), secondMenuItemCase.getStringId());
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(firstMenuItemCase.getStringId()));
    }

    @Test
    public void createOrIgnoreMenuItemTest() throws TransitionNotExecutableException {
        assertThrows(IllegalArgumentException.class, () -> menuItemService.createOrIgnoreMenuItem(null));

        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(CustomViewTemplate.IDENTIFIER,
                uriService.getRoot().getUriPath(), "test");
        assertTrue(menuItemBodyOpt.isPresent());

        Case firstMenuItemCase = menuItemService.createOrIgnoreMenuItem(menuItemBodyOpt.get());
        Case secondMenuItemCase = menuItemService.createOrIgnoreMenuItem(menuItemBodyOpt.get());

        assertEquals(firstMenuItemCase.getStringId(), secondMenuItemCase.getStringId());
    }

    @Test
    public void findMenuItemByIdentifierTest() throws TransitionNotExecutableException {
        assertNull(menuItemService.findMenuItem(null));
        assertNull(menuItemService.findMenuItem("wrong"));

        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(CustomViewTemplate.IDENTIFIER);
        assertTrue(menuItemBodyOpt.isPresent());
        MenuItemBody menuItemBody = menuItemBodyOpt.get();
        menuItemBody.setUri(uriService.getRoot().getUriPath());
        String identifier = "test";
        menuItemBody.setIdentifier(identifier);

        Case createdMenuItemCase = menuItemService.createMenuItem(menuItemBody);
        Case foundMenuItemCase = menuItemService.findMenuItem(identifier);
        assertEquals(createdMenuItemCase.getStringId(), foundMenuItemCase.getStringId());
    }

    @Test
    public void findFolderCaseTest() throws TransitionNotExecutableException {
        assertThrows(IllegalArgumentException.class, () -> menuItemService.findFolderCase(null));
        assertNull(menuItemService.findFolderCase(new UriNode()));

        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(FolderTemplate.IDENTIFIER, "/folderik", "test");
        assertTrue(menuItemBodyOpt.isPresent());

        Case createdMenuItemCase = menuItemService.createMenuItem(menuItemBodyOpt.get());
        String parentFolderCaseId = MenuItemUtils.getCaseIdFromCaseRef(createdMenuItemCase, MenuItemConstants.FIELD_PARENT_ID);
        assertNotNull(parentFolderCaseId);
        UriNode node = uriService.findById(createdMenuItemCase.getUriNodeId());
        Case folderCase = menuItemService.findFolderCase(node);
        assertEquals(parentFolderCaseId, folderCase.getStringId());
    }

    @Test
    public void existsMenuItemTest() throws TransitionNotExecutableException {
        assertFalse(menuItemService.existsMenuItem(null));
        assertFalse(menuItemService.existsMenuItem("wrong"));

        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(CustomViewTemplate.IDENTIFIER);
        assertTrue(menuItemBodyOpt.isPresent());
        MenuItemBody menuItemBody = menuItemBodyOpt.get();
        menuItemBody.setUri(uriService.getRoot().getUriPath());
        String identifier = "test";
        menuItemBody.setIdentifier(identifier);

        menuItemService.createMenuItem(menuItemBody);
        assertTrue(menuItemService.existsMenuItem(identifier));
    }

    @Test
    public void moveItemTest() throws TransitionNotExecutableException {
        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(CustomViewTemplate.IDENTIFIER);
        assertTrue(menuItemBodyOpt.isPresent());
        MenuItemBody menuItemBody = menuItemBodyOpt.get();
        menuItemBody.setUri("/netgrif/test");
        menuItemBody.setIdentifier("new_menu_item");
        Case newMenuItemCase = menuItemService.createMenuItem(menuItemBody);
        menuItemBody.setUri("/netgrif2/test2");
        menuItemBody.setIdentifier("new_menu_item2");
        Case newMenuItem2Case = menuItemService.createMenuItem(menuItemBody);

        assertThrows(IllegalArgumentException.class, () -> menuItemService.moveItem(null, "/mypath"));
        Case finalNewMenuItemCase = newMenuItemCase;
        assertThrows(IllegalArgumentException.class, () -> menuItemService.moveItem(finalNewMenuItemCase, null));

        menuItemService.moveItem(newMenuItemCase, "/netgrif2");

        newMenuItemCase = workflowService.findOne(newMenuItemCase.getStringId());
        UriNode netgrif2Node = uriService.findByUri("/netgrif2");
        assertEquals(netgrif2Node.getStringId(), newMenuItemCase.getUriNodeId());
        Case netgrif2FolderCase = findMenuItem("netgrif2");
        List<String> netgrif2ChildIds = MenuItemUtils.getCaseIdsFromCaseRef(netgrif2FolderCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(netgrif2ChildIds);
        assertEquals(2, netgrif2ChildIds.size());
        assertTrue(netgrif2ChildIds.contains(newMenuItemCase.getStringId()));

        Case finalNetgrif2FolderCase = netgrif2FolderCase;
        assertThrows(IllegalArgumentException.class, () -> menuItemService.moveItem(finalNetgrif2FolderCase, "/netgrif2/cyclic"));

        menuItemService.moveItem(netgrif2FolderCase, "/netgrif/test3");
        Case test3FolderCase = findMenuItem("test3");
        Case netgrifFolderCase = findMenuItem("netgrif");

        String test3ParentCaseId = MenuItemUtils.getCaseIdFromCaseRef(test3FolderCase, MenuItemConstants.FIELD_PARENT_ID);
        assertNotNull(test3ParentCaseId);
        assertEquals(test3ParentCaseId, netgrifFolderCase.getStringId());

        List<String> netgrifFolderChildIds = MenuItemUtils.getCaseIdsFromCaseRef(netgrifFolderCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(netgrifFolderChildIds);
        assertTrue(netgrifFolderChildIds.contains(test3FolderCase.getStringId()));

        netgrif2FolderCase = workflowService.findOne(netgrif2FolderCase.getStringId());
        UriNode test3Node = uriService.findByUri("/netgrif/test3");
        assertEquals(test3Node.getStringId(), netgrif2FolderCase.getUriNodeId());
        assertEquals("/netgrif/test3/netgrif2", netgrif2FolderCase.getFieldValue(MenuItemConstants.FIELD_NODE_PATH));
        netgrif2ChildIds = MenuItemUtils.getCaseIdsFromCaseRef(netgrif2FolderCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(netgrif2ChildIds);
        assertEquals(2, netgrif2ChildIds.size());

        Case test2FolderCase = findMenuItem("test2");
        netgrif2Node = uriService.findByUri("/netgrif/test3/netgrif2");
        assertEquals(netgrif2Node.getStringId(), test2FolderCase.getUriNodeId());
        assertEquals("/netgrif/test3/netgrif2/test2", test2FolderCase.getFieldValue(MenuItemConstants.FIELD_NODE_PATH));

        newMenuItem2Case = workflowService.findOne(newMenuItem2Case.getStringId());
        UriNode test2Node = uriService.findByUri("/netgrif/test3/netgrif2/test2");
        assertEquals(test2Node.getStringId(), newMenuItem2Case.getUriNodeId());
    }

    @Test
    public void duplicateFolderItemTest() throws TransitionNotExecutableException {
        String starterUri = "/netgrif/test";
        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(TabbedCaseViewTemplate.IDENTIFIER);
        assertTrue(menuItemBodyOpt.isPresent());
        MenuItemBody menuItemBody = menuItemBodyOpt.get();
        menuItemBody.setUri(starterUri);
        menuItemBody.setIdentifier("new_menu_item");
        menuItemService.createMenuItem(menuItemBody);
        Case originFolderCase = findMenuItem("test");

        String newTitle = "New title";
        String newIdentifier = "new_identifier";

        String duplicateTaskId = MenuItemUtils.findTaskIdInCase(originFolderCase, "duplicate_item");
        taskService.assignTask(duplicateTaskId);

        originFolderCase.getDataField(MenuItemConstants.FIELD_DUPLICATE_TITLE).setValue(new I18nString(""));
        originFolderCase.getDataField(MenuItemConstants.FIELD_DUPLICATE_IDENTIFIER).setValue(newIdentifier);
        originFolderCase = workflowService.save(originFolderCase);
        assertThrows(IllegalArgumentException.class, () -> taskService.finishTask(duplicateTaskId));

        originFolderCase.getDataField(MenuItemConstants.FIELD_DUPLICATE_TITLE).setValue(new I18nString(newTitle));
        originFolderCase.getDataField(MenuItemConstants.FIELD_DUPLICATE_IDENTIFIER).setValue("new_menu_item");
        originFolderCase = workflowService.save(originFolderCase);
        assertThrows(IllegalArgumentException.class, () -> taskService.finishTask(duplicateTaskId));

        originFolderCase.getDataField(MenuItemConstants.FIELD_DUPLICATE_TITLE).setValue(new I18nString(newTitle));
        originFolderCase.getDataField(MenuItemConstants.FIELD_DUPLICATE_IDENTIFIER).setValue(newIdentifier);
        originFolderCase = workflowService.save(originFolderCase);
        taskService.finishTask(duplicateTaskId);

        Case duplicatedFolderCase = findMenuItem(newIdentifier);
        assertNotNull(duplicatedFolderCase);

        UriNode leafNode = uriService.findByUri("/netgrif/" + newIdentifier);

        assertNotNull(leafNode);
        assertEquals(duplicatedFolderCase.getUriNodeId(), originFolderCase.getUriNodeId());
        assertEquals(new I18nString(""), duplicatedFolderCase.getFieldValue(MenuItemConstants.FIELD_DUPLICATE_TITLE));
        assertEquals("", duplicatedFolderCase.getFieldValue(MenuItemConstants.FIELD_DUPLICATE_IDENTIFIER));
        assertEquals(newTitle, duplicatedFolderCase.getTitle());
        assertEquals(new I18nString(newTitle), duplicatedFolderCase.getFieldValue(MenuItemConstants.FIELD_MENU_NAME));
        assertEquals(newIdentifier, duplicatedFolderCase.getFieldValue(MenuItemConstants.FIELD_IDENTIFIER));
        assertEquals("/netgrif/" + newIdentifier, duplicatedFolderCase.getFieldValue(MenuItemConstants.FIELD_NODE_PATH));
        List<String> duplicatedChildIds = MenuItemUtils.getCaseIdsFromCaseRef(duplicatedFolderCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(duplicatedChildIds);
        assertEquals(0, duplicatedChildIds.size());
        assertTrue((Boolean) duplicatedFolderCase.getFieldValue(MenuItemConstants.FIELD_IS_FOLDER));
        assertEquals(1, duplicatedFolderCase.getActivePlaces().get("initialized"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void duplicateLeafItemTest() throws TransitionNotExecutableException {
        String starterUri = "/netgrif/test";
        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(TabbedCaseViewTemplate.IDENTIFIER);
        assertTrue(menuItemBodyOpt.isPresent());
        MenuItemBody menuItemBody = menuItemBodyOpt.get();
        menuItemBody.setUri(starterUri);
        menuItemBody.setIdentifier("new_menu_item");
        Case originLeafItemCase = menuItemService.createMenuItem(menuItemBody);

        String newTitle = "New title";
        String newIdentifier = "new_identifier";

        Case duplicatedLeafItemCase = menuItemService.duplicateItem(originLeafItemCase, new I18nString(newTitle), newIdentifier);
        assertEquals(duplicatedLeafItemCase.getUriNodeId(), originLeafItemCase.getUriNodeId());
        assertEquals(duplicatedLeafItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE),
                originLeafItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE));

        String duplicatedCaseViewId = MenuItemUtils.getCaseIdFromCaseRef(duplicatedLeafItemCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertNotNull(duplicatedCaseViewId);
        String originCaseViewId = MenuItemUtils.getCaseIdFromCaseRef(originLeafItemCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertNotNull(originCaseViewId);
        assertNotEquals(duplicatedCaseViewId, originCaseViewId);

        List<String> duplicatedFormValue = (List<String>) duplicatedLeafItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_FORM);
        assertNotNull(duplicatedFormValue);
        assertEquals(1, duplicatedFormValue.size());
        List<String> originFormValue = (List<String>) originLeafItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_FORM);
        assertNotNull(originFormValue);
        assertEquals(1, originFormValue.size());
        assertNotEquals(duplicatedFormValue.get(0), originFormValue.get(0));

        List<String> duplicatedAllFormValue = (List<String>) duplicatedLeafItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_ALL_DATA_FORM);
        assertNotNull(duplicatedAllFormValue);
        assertEquals(1, duplicatedAllFormValue.size());
        List<String> originAllFormValue = (List<String>) originLeafItemCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_ALL_DATA_FORM);
        assertNotNull(originAllFormValue);
        assertEquals(1, originAllFormValue.size());
        assertNotEquals(duplicatedAllFormValue.get(0), originAllFormValue.get(0));

        Case duplicatedCaseViewCase = workflowService.findOne(duplicatedCaseViewId);
        Case originCaseViewCase = workflowService.findOne(originCaseViewId);
        assertEquals(duplicatedCaseViewCase.getProcessIdentifier(), originCaseViewCase.getProcessIdentifier());

        assertEquals(duplicatedCaseViewCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE),
                originCaseViewCase.getFieldValue(MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE));

        String duplicatedTaskViewId = MenuItemUtils.getCaseIdFromCaseRef(duplicatedCaseViewCase, ViewConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertNotNull(duplicatedTaskViewId);
        String originTaskViewId = MenuItemUtils.getCaseIdFromCaseRef(originCaseViewCase, ViewConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertNotNull(originTaskViewId);
        assertNotEquals(duplicatedTaskViewId, originTaskViewId);
    }

    @Test
    public void removeChildItemFromParentTest() throws TransitionNotExecutableException {
        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(CustomViewTemplate.IDENTIFIER);
        assertTrue(menuItemBodyOpt.isPresent());
        MenuItemBody menuItemBody = menuItemBodyOpt.get();
        menuItemBody.setUri("/folderik");
        menuItemBody.setIdentifier("test");

        Case testItemCase = menuItemService.createMenuItem(menuItemBody);

        Case folderikCase = findMenuItem("folderik");
        assertNotNull(folderikCase);
        List<String> childrenCaseIds = MenuItemUtils.getCaseIdsFromCaseRef(folderikCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(childrenCaseIds);
        assertEquals(1, childrenCaseIds.size());
        assertTrue(childrenCaseIds.contains(testItemCase.getStringId()));
        assertTrue((Boolean) folderikCase.getFieldValue(MenuItemConstants.FIELD_IS_FOLDER));

        folderikCase = menuItemService.removeChildItemFromParent(folderikCase.getStringId(), testItemCase);

        childrenCaseIds = MenuItemUtils.getCaseIdsFromCaseRef(folderikCase, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(childrenCaseIds);
        assertEquals(0, childrenCaseIds.size());
        assertTrue((Boolean) folderikCase.getFieldValue(MenuItemConstants.FIELD_IS_FOLDER));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void handleConfigurationTemplateTest() throws TransitionNotExecutableException {
        // tabbed case view
        Case menuItemCase = workflowService.createCaseByIdentifier(MenuItemConstants.PROCESS_IDENTIFIER, "", "",
                userService.getSystem().transformToLoggedUser()).getCase();

        menuItemCase = dataService.setData(MenuItemUtils.findTaskIdInCase(menuItemCase, MenuItemConstants.TRANS_INIT_ID),
                ImportHelper.populateDataset(
                        Map.of(MenuItemConstants.FIELD_CONFIGURATION_TEMPLATES,
                                Map.of("type", FieldType.ENUMERATION_MAP.getName(), "value", TabbedCaseViewTemplate.IDENTIFIER))
                )
        ).getCase();

        ConfigurationTemplateOutcome outcome = menuItemService.handleConfigurationTemplate(menuItemCase);

        assertTrue((Boolean) outcome.getMapping().get(MenuItemConstants.FIELD_USE_TABBED_VIEW));
        assertFalse((Boolean) outcome.getMapping().get(MenuItemConstants.FIELD_USE_CUSTOM_VIEW));
        assertFalse((Boolean) outcome.getMapping().get(MenuItemConstants.FIELD_IS_FOLDER));
        assertEquals(MenuItemViewType.CASE_VIEW.getIdentifier(), outcome.getMapping().get(MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE));
        List<String> viewCaseIdAsList = (List<String>) outcome.getMapping().get(MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertEquals(1, viewCaseIdAsList.size());
        Case viewCase = workflowService.findOne(viewCaseIdAsList.get(0));
        assertEquals("case_view_configuration", viewCase.getProcessIdentifier());
        List<String> viewFormTaskIdAsList = (List<String>) outcome.getMapping().get(MenuItemConstants.FIELD_VIEW_CONFIGURATION_FORM);
        assertEquals(1, viewFormTaskIdAsList.size());
        assertEquals(MenuItemUtils.findTaskIdInCase(viewCase, ViewConstants.TRANS_SETTINGS_ID), viewFormTaskIdAsList.get(0));
        List<String> viewAllFormTaskIdAsList = (List<String>) outcome.getMapping().get(MenuItemConstants.FIELD_VIEW_CONFIGURATION_ALL_DATA_FORM);
        assertEquals(1, viewAllFormTaskIdAsList.size());
        assertEquals(MenuItemUtils.findTaskIdInCase(viewCase, ViewConstants.TRANS_ALL_MENU_DATA_ID), viewAllFormTaskIdAsList.get(0));

        // folder item
        menuItemCase = workflowService.createCaseByIdentifier(MenuItemConstants.PROCESS_IDENTIFIER, "", "",
                userService.getSystem().transformToLoggedUser()).getCase();

        menuItemCase = dataService.setData(MenuItemUtils.findTaskIdInCase(menuItemCase, MenuItemConstants.TRANS_INIT_ID),
                ImportHelper.populateDataset(
                        Map.of(MenuItemConstants.FIELD_CONFIGURATION_TEMPLATES,
                                Map.of("type", FieldType.ENUMERATION_MAP.getName(), "value", FolderTemplate.IDENTIFIER))
                )
        ).getCase();

        outcome = menuItemService.handleConfigurationTemplate(menuItemCase);
        assertFalse((Boolean) outcome.getMapping().get(MenuItemConstants.FIELD_USE_TABBED_VIEW));
        assertFalse((Boolean) outcome.getMapping().get(MenuItemConstants.FIELD_USE_CUSTOM_VIEW));
        assertTrue((Boolean) outcome.getMapping().get(MenuItemConstants.FIELD_IS_FOLDER));

        // custom view
        menuItemCase = workflowService.createCaseByIdentifier(MenuItemConstants.PROCESS_IDENTIFIER, "", "",
                userService.getSystem().transformToLoggedUser()).getCase();

        menuItemCase = dataService.setData(MenuItemUtils.findTaskIdInCase(menuItemCase, MenuItemConstants.TRANS_INIT_ID),
                ImportHelper.populateDataset(
                        Map.of(MenuItemConstants.FIELD_CONFIGURATION_TEMPLATES,
                                Map.of("type", FieldType.ENUMERATION_MAP.getName(), "value", CustomViewTemplate.IDENTIFIER))
                )
        ).getCase();

        outcome = menuItemService.handleConfigurationTemplate(menuItemCase);
        assertFalse((Boolean) outcome.getMapping().get(MenuItemConstants.FIELD_USE_TABBED_VIEW));
        assertTrue((Boolean) outcome.getMapping().get(MenuItemConstants.FIELD_USE_CUSTOM_VIEW));
        assertFalse((Boolean) outcome.getMapping().get(MenuItemConstants.FIELD_IS_FOLDER));
    }

    @Test
    public void getAvailableViewsAsOptionsByIsPrimaryTest() {
        
        Map<String, I18nString> options = menuItemService.getAvailableViewsAsOptions(true, true);
        assertNotNull(options);
        assertEquals(4, options.size());
        assertTrue(options.containsKey(MenuItemViewType.CASE_VIEW.getIdentifier()));
        assertTrue(options.containsKey(MenuItemViewType.TASK_VIEW.getIdentifier()));
        assertTrue(options.containsKey(MenuItemViewType.TABBED_TICKET_VIEW.getIdentifier()));
        assertTrue(options.containsKey(MenuItemViewType.SINGLE_TASK_VIEW.getIdentifier()));

        options = menuItemService.getAvailableViewsAsOptions(true, false);
        assertNotNull(options);
        assertEquals(0, options.size());

        options = menuItemService.getAvailableViewsAsOptions(false, false);
        assertNotNull(options);
        assertEquals(0, options.size());

        options = menuItemService.getAvailableViewsAsOptions(false, true);
        assertNotNull(options);
        assertEquals(3, options.size());
        assertTrue(options.containsKey(MenuItemViewType.CASE_VIEW.getIdentifier()));
        assertTrue(options.containsKey(MenuItemViewType.TASK_VIEW.getIdentifier()));
        assertTrue(options.containsKey(MenuItemViewType.SINGLE_TASK_VIEW.getIdentifier()));
    }

    @Test
    public void getAvailableViewsAsOptionsByViewIdentifierTest() {
        Map<String, I18nString> options = menuItemService.getAvailableViewsAsOptions(true, MenuItemViewType.CASE_VIEW.getIdentifier());
        assertNotNull(options);
        assertEquals(1, options.size());
        assertTrue(options.containsKey(MenuItemViewType.TASK_VIEW.getIdentifier()));

        options = menuItemService.getAvailableViewsAsOptions(true, MenuItemViewType.TABBED_TICKET_VIEW.getIdentifier());
        assertNotNull(options);
        assertEquals(1, options.size());
        assertTrue(options.containsKey(MenuItemViewType.SINGLE_TASK_VIEW.getIdentifier()));

        options = menuItemService.getAvailableViewsAsOptions(false, MenuItemViewType.TABBED_TICKET_VIEW.getIdentifier());
        assertNotNull(options);
        assertEquals(1, options.size());
        assertTrue(options.containsKey(MenuItemViewType.SINGLE_TASK_VIEW.getIdentifier()));

        options = menuItemService.getAvailableViewsAsOptions(true, MenuItemViewType.TASK_VIEW.getIdentifier());
        assertNotNull(options);
        assertEquals(0, options.size());
    }

    @Test
    void testRemoveMenuItem() throws TransitionNotExecutableException, InterruptedException {
        Optional<MenuItemBody> menuItemBodyOpt = MenuItemTemplateHolder.get(TabbedCaseViewTemplate.IDENTIFIER);
        assertTrue(menuItemBodyOpt.isPresent());
        MenuItemBody menuItemBody = menuItemBodyOpt.get();
        menuItemBody.setUri("/netgrif/test");
        menuItemBody.setIdentifier("new_menu_item");
        Case leafItemCase = menuItemService.createMenuItem(menuItemBody);

        Case testFolder = findMenuItem("test");
        String netgrifFolderId = MenuItemUtils.getCaseIdFromCaseRef(testFolder, MenuItemConstants.FIELD_PARENT_ID);

        Case netgrifFolder = workflowService.findOne(netgrifFolderId);
        List<String> childIds = MenuItemUtils.getCaseIdsFromCaseRef(netgrifFolder, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(childIds);
        assertTrue(childIds.contains(testFolder.getStringId()));
        assertDoesNotThrow(() -> workflowService.findOne(testFolder.getStringId()));
        assertDoesNotThrow(() -> workflowService.findOne(leafItemCase.getStringId()));
        String tabbedCaseViewId = MenuItemUtils.getCaseIdFromCaseRef(leafItemCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertNotNull(tabbedCaseViewId);
        Case tabbedCaseView = workflowService.findOne(tabbedCaseViewId);
        String tabbedTaskViewId = MenuItemUtils.getCaseIdFromCaseRef(tabbedCaseView, CaseViewConstants.FIELD_VIEW_CONFIGURATION_ID);
        assertNotNull(tabbedTaskViewId);
        assertDoesNotThrow(() -> workflowService.findOne(tabbedTaskViewId));

        workflowService.deleteCase(testFolder);
        Thread.sleep(2000);
        netgrifFolder = workflowService.findOne(netgrifFolderId);
        List<String> updatedChildIds = MenuItemUtils.getCaseIdsFromCaseRef(netgrifFolder, MenuItemConstants.FIELD_CHILD_ITEM_IDS);
        assertNotNull(updatedChildIds);
        assertFalse(updatedChildIds.contains(testFolder.getStringId()));
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(testFolder.getStringId()));
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(leafItemCase.getStringId()));
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(tabbedCaseViewId));
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(tabbedTaskViewId));
    }

    private String getTaskId(Case useCase, String transId) {
        return useCase.getTasks().stream()
                .filter((taskPair -> taskPair.getTransition().equals(transId)))
                .map(TaskPair::getTask)
                .findFirst().orElse(null);
    }

    private Case findMenuItem(String identifier) {
        Query query = Query.query(
                Criteria.where("processIdentifier").is(MenuItemConstants.PROCESS_IDENTIFIER)
                        .and(String.format("dataSet.%s.value", MenuItemConstants.FIELD_IDENTIFIER)).is(identifier)
        );
        query.withHint(MenuItemConstants.IDENTIFIER_INDEX_NAME);
        List<Case> caseAsList = mongoTemplate.find(query, Case.class);
        Optional<Case> caseOptional = caseAsList.stream().findFirst();
        return caseOptional.map(aCase -> workflowService.findOne(aCase.getStringId())).orElse(null);
    }
}
