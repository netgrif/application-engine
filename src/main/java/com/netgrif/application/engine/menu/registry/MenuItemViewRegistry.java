package com.netgrif.application.engine.menu.registry;

import com.netgrif.application.engine.menu.domain.MenuItemView;
import com.netgrif.application.engine.menu.registry.interfaces.IMenuItemViewRegistry;
import com.netgrif.application.engine.menu.registry.throwable.DuplicateViewException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        if (this.views.containsKey(view.getIdentifier())) {
            throw new DuplicateViewException(view.getIdentifier());
        }
        this.views.put(view.getIdentifier(), view);
        log.debug("Registered menu item view [{}] with identifier [{}]", view.getName().getDefaultValue(), view.getIdentifier());
    }

    /**
     * todo javadoc
     * */
    @Override
    public void unregisterView(String identifier) {
        this.views.remove(identifier);
    }

    /**
     * todo javadoc
     * */
    @Override
    public void unregisterAllViews() {
        Set<String> viewIds = new HashSet<>(this.views.keySet());
        for (String viewId : viewIds) {
            this.views.remove(viewId);
        }
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
     * Gets all views, that are tabbed or non-tabbed
     *
     * @param isTabbed if true, only tabbed values will be returned
     * @param isPrimary if true, only views accessible directly from the menu_item will be returned
     *
     * @return List of views based on input parameters
     * */
    @Override
    public List<MenuItemView> getAllByIsTabbedAndIsPrimary(boolean isTabbed, boolean isPrimary) {
        return this.views.values().stream()
                .filter(menuItemView -> menuItemView.isTabbed() == isTabbed && menuItemView.isPrimary() == isPrimary)
                .collect(Collectors.toList());
    }

    /**
     * Gets all views, that are tabbed or non-tabbed and are defined in parent view as {@link MenuItemView#getAllowedAssociatedViews()}
     *
     * @param isTabbed if true, set of views is reduced to only tabbed views
     * @param parentIdentifier identifier of the view, that contains returned views in {@link MenuItemView#getAllowedAssociatedViews()}
     *
     * @return List of views based on input parameters
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
