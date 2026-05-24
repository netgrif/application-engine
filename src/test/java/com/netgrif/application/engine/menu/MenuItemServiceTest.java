package com.netgrif.application.engine.menu;

import com.mongodb.client.MongoCollection;
import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.MenuItemConstants;
import com.netgrif.application.engine.menu.domain.MenuItemViewType;
import com.netgrif.application.engine.menu.domain.configurations.*;
import com.netgrif.application.engine.menu.domain.templates.TabbedCaseViewTemplate;
import com.netgrif.application.engine.menu.domain.templates.Template;
import com.netgrif.application.engine.menu.service.MenuItemService;
import com.netgrif.application.engine.menu.service.MenuItemTemplateHolder;
import com.netgrif.application.engine.menu.utils.MenuItemUtils;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.domain.TaskPair;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Locale;
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
    private SuperCreator superCreator;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private IUriService uriService;

    @Autowired
    private IWorkflowService workflowService;
    
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
            Optional<Template> templateOpt = MenuItemTemplateHolder.get(templateIdentifier);
            assertTrue(templateOpt.isPresent());
            MenuItemBody menuItemBody = templateOpt.get().getTemplate();
            menuItemBody.setUri(uriService.getRoot().getUriPath());
            menuItemBody.setIdentifier(templateIdentifier);
            menuItemBody.setConfigurationTemplateIdentifier(templateIdentifier);
            try {
                createByTemplateAndAssert(menuItemBody);
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
        Optional<Template> templateOpt = MenuItemTemplateHolder.get(TabbedCaseViewTemplate.IDENTIFIER);
        assertTrue(templateOpt.isPresent());
        MenuItemBody menuItemBody = templateOpt.get().getTemplate();
        menuItemBody.setUri("/netgrif/test");
        menuItemBody.setIdentifier("new_menu_item");
        menuItemBody.setMenuIcon("device_hub");
        assertNotNull(menuItemBody.getView());
        assertNotNull(menuItemBody.getView().getFilterBody());
        menuItemBody.getView().getFilterBody().setAllowedNets(List.of("menu_item"));
        menuItemBody.getView().getFilterBody().setQuery("processIdentifier:menu_item");
        menuItemBody.getView().getFilterBody().setType("Case");
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
        assertEquals("Case", filterDataField.getFilterMetadata().get("filterType"));
        assertTrue(filterDataField.getAllowedNets().size() == 1 && filterDataField.getAllowedNets().contains("menu_item"));
        assertEquals("processIdentifier:menu_item", filterDataField.getValue());

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

        Case rootFolder = findMenuItem("");

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
    }

    @Test
    public void updateMenuItemTest() {
        // todo
    }

    @Test
    public void createOrUpdateMenuItemTest() {
        // todo
    }

    @Test
    public void createOrIgnoreMenuItemTest() {
        // todo
    }

    @Test
    public void findMenuItemByIdentifierTest() {
        // todo
    }

    @Test
    public void findMenuItemByUriAndNameTest() {
        // todo
    }

    @Test
    public void findFolderCaseTest() {
        // todo
    }

    @Test
    public void existsMenuItemTest() {
        // todo
    }

    @Test
    public void moveItemTest() {
        // todo
    }

    @Test
    public void duplicateItemTest() {
        // todo
    }

    @Test
    public void removeChildItemFromParentTest() {
        // todo
    }

    @Test
    public void getMenuItemDataTest() throws TransitionNotExecutableException {
        assertThrows(IllegalArgumentException.class, () -> menuItemService.getMenuItemData("wrongCaseId", Locale.getDefault()));

        Case menuItemCase = createDefaultMenuItem("my_menu_item",
                new I18nString("This is name", Map.of("sk", "Toto je nazov")));

        login();
        List<DataGroup> result = menuItemService.getMenuItemData(menuItemCase.getStringId(), Locale.getDefault());
        assertTrue(result != null && !result.isEmpty());
    }

    @Test
    public void handleConfigurationTemplateTest() {
        // todo
    }

    @Test
    public void getAvailableViewsAsOptionsByIsPrimaryTest() {
        // todo
    }

    @Test
    public void getAvailableViewsAsOptionsByViewIdentifierTest() {
        // todo
    }

    private Case createDefaultMenuItem(String identifier, I18nString name) throws TransitionNotExecutableException {
        FilterBody filterBody = new FilterBody();
        filterBody.setQuery("processIdentifier:process1");
        filterBody.setType("Case");
        filterBody.setAllowedNets(List.of("process1"));
        filterBody.setIcon("home");
        filterBody.setVisibility("private");

        CaseViewBody caseView = new CaseViewBody();
        caseView.setFilterBody(filterBody);
        caseView.setRequireTitleInCreation(false);
        caseView.setChainedView(new TaskViewBody());

        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setUri("/");
        menuItemBody.setIdentifier(identifier);
        menuItemBody.setMenuIcon("home");
        menuItemBody.setMenuName(name);
        menuItemBody.setTabIcon("folder");
        menuItemBody.setTabName(name);
        menuItemBody.setView(caseView);

        return menuItemService.createMenuItem(menuItemBody);
    }

    private void login() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(superCreator.getLoggedSuper(), null));
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
