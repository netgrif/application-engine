package com.netgrif.application.engine.menu;

import com.mongodb.client.MongoCollection;
import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.MenuItemConstants;
import com.netgrif.application.engine.menu.domain.configurations.CaseViewBody;
import com.netgrif.application.engine.menu.domain.configurations.TaskViewBody;
import com.netgrif.application.engine.menu.domain.configurations.ViewConstants;
import com.netgrif.application.engine.menu.domain.templates.Template;
import com.netgrif.application.engine.menu.service.MenuItemService;
import com.netgrif.application.engine.menu.service.MenuItemTemplateHolder;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.TaskPair;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
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
    public void createMenuItemTest() {
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
                createAndAssertMenuItem(menuItemBody);
            } catch (TransitionNotExecutableException e) {
                throw new RuntimeException(e);
            }
        });

        // todo 23 detailny test pre zlozite view (porovnat aj s MenuItemApiTest)
    }

    @SuppressWarnings("unchecked")
    private void createAndAssertMenuItem(MenuItemBody menuItemBody) throws TransitionNotExecutableException {
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
}
