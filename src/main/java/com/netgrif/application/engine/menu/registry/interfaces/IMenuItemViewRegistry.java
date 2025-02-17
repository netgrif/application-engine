package com.netgrif.application.engine.menu.registry.interfaces;

import com.netgrif.application.engine.menu.domain.MenuItemView;

import java.util.Map;
import java.util.List;

public interface IMenuItemViewRegistry {

    void registerView(MenuItemView view);
    void unregisterView(String identifier);
    void unregisterAllViews();
    MenuItemView getViewByIdentifier(String identifier);
    Map<String, MenuItemView> getAllViews();
    List<MenuItemView> getAllByIsTabbedAndIsPrimary(boolean isTabbed, boolean isPrimary);
    List<MenuItemView> getAllByIsTabbedAndParentIdentifier(boolean isTabbed, String parentIdentifier);

}
