package com.netgrif.application.engine.menu;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.CaseViewBody;
import com.netgrif.application.engine.menu.domain.configurations.TaskViewBody;
import com.netgrif.application.engine.menu.service.interfaces.IMenuItemService;
import com.netgrif.application.engine.petrinet.domain.DataGroup;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class MenuItemServiceTest {
    
    @Autowired
    private TestHelper testHelper;
    
    @Autowired
    private IMenuItemService menuItemService;

    @Autowired
    private SuperCreator superCreator;
    
    @BeforeEach
    public void beforeEach() {
        testHelper.truncateDbs();
    }

    @Test
    public void createFilterTest() {
        // todo
    }

    @Test
    public void updateFilterTest() {
        // todo
    }

    @Test
    public void createMenuItemTest() {
        // todo
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
    public void getAvailableViewsAsOptionsByIsPrimaryTest() {
        // todo
    }

    @Test
    public void getAvailableViewsAsOptionsByViewIdentifierTest() {
        // todo
    }

    private Case createDefaultMenuItem(String identifier, I18nString name) throws TransitionNotExecutableException {
        FilterBody filterBody = new FilterBody();
        filterBody.setTitle(new I18nString("My case view filter"));
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
}
