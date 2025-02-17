package com.netgrif.application.engine.menu.registry;

import com.netgrif.application.engine.menu.domain.MenuItemView;
import com.netgrif.application.engine.menu.registry.interfaces.IMenuItemViewRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MenuItemViewRegistry implements IMenuItemViewRegistry {

    /**
     * todo javadoc
     * */
    private final Map<String, MenuItemView> views;

    public MenuItemViewRegistry() {
        this.views = new ConcurrentHashMap<>();
    }

    /**
     * todo javadoc
     * */
    @Override
    public void registerView(@Validated MenuItemView view) {
        this.views.put(view.getIdentifier(), view);
        log.debug("Registered menu item view [{}] with identifier [{}]", view.getName().getDefaultValue(), view.getIdentifier());
    }

    /**
     * todo javadoc
     * */
    @Override
    public MenuItemView getViewByIdentifier(String identifier) {
        return this.views.get(identifier);
    }

    /**
     * todo javadoc
     * */
    @Override
    public Map<String, MenuItemView> getAllViews() {
        return this.views;
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<MenuItemView> getAllByIsTabbedAndIsPrimary(boolean isTabbed, boolean isPrimary) {
        return this.views.values().stream()
                .filter(menuItemView -> menuItemView.isTabbed() == isTabbed && menuItemView.isPrimary() == isPrimary)
                .collect(Collectors.toList());
    }

    /**
     * todo javadoc
     * */
    @Override
    public List<MenuItemView> getAllByIsTabbedAndParentIdentifier(boolean isTabbed, String parentIdentifier) {
        MenuItemView parentView = getViewByIdentifier(parentIdentifier);
        return this.views.values().stream()
                .filter(menuItemView -> menuItemView.isTabbed() == isTabbed
                        && parentView.getAllowedAssociatedViews().contains(menuItemView.getIdentifier()))
                .collect(Collectors.toList());
    }
}
