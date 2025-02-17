package com.netgrif.application.engine.menuitem;

import com.netgrif.application.engine.menu.domain.MenuItemView;
import com.netgrif.application.engine.menu.registry.interfaces.IMenuItemViewRegistry;
import com.netgrif.application.engine.menu.registry.throwable.DuplicateViewException;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class MenuItemViewRegistryTest {

    @Autowired
    private IMenuItemViewRegistry viewRegistry;

    @BeforeEach
    public void before() {
        viewRegistry.unregisterAllViews();
    }

    @Test
    public void testRegisterView() {
        assert viewRegistry.getAllViews().isEmpty();

        MenuItemView view = buildView("test_view", true, true, List.of());
        viewRegistry.registerView(view);
        assert viewRegistry.getAllViews().size() == 1;

        assertThrows(DuplicateViewException.class, () -> viewRegistry.registerView(view));
    }

    @Test
    public void testUnregisterView() {
        MenuItemView view = buildView("test_view", true, true, List.of());
        viewRegistry.registerView(view);
        assert viewRegistry.getAllViews().size() == 1;
        viewRegistry.unregisterView("test_view");
        assert viewRegistry.getAllViews().isEmpty();
    }

    @Test
    public void testGetViewByIdentifier() {
        MenuItemView view = buildView("test_view", true, true, List.of());
        viewRegistry.registerView(view);
        assert viewRegistry.getViewByIdentifier("test_view") != null;
    }

    @Test
    public void testGetAllByIsTabbedAndIsPrimary() {
        MenuItemView view1 = buildView("test_view1", true, true, List.of());
        MenuItemView view2 = buildView("test_view2", true, false, List.of());
        MenuItemView view3 = buildView("test_view3", false, true, List.of());
        viewRegistry.registerView(view1);
        viewRegistry.registerView(view2);
        viewRegistry.registerView(view3);
        assert viewRegistry.getAllByIsTabbedAndIsPrimary(true, true).size() == 1;
    }

    @Test
    public void testGetAllByIsTabbedAndParentIdentifier() {
        MenuItemView view1 = buildView("test_view1", true, true, List.of("test_view2", "test_view3"));
        MenuItemView view2 = buildView("test_view2", true, true, List.of());
        MenuItemView view3 = buildView("test_view3", false, true, List.of());
        viewRegistry.registerView(view1);
        viewRegistry.registerView(view2);
        viewRegistry.registerView(view3);
        assert viewRegistry.getAllByIsTabbedAndParentIdentifier(true, "test_view1").size() == 1;
    }

    private MenuItemView buildView(String identifier, boolean isTabbed, boolean isPrimary, List<String> associatedViewIds) {
        return MenuItemView.with()
                .name(new I18nString("Test view"))
                .identifier(identifier)
                .allowedAssociatedViews(associatedViewIds)
                .isTabbed(isTabbed)
                .isPrimary(isPrimary)
                .build();
    }
}
