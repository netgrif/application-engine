package com.netgrif.application.engine.menu.services.interfaces;

import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.MenuItemView;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;

import java.util.Map;
import java.util.stream.Collectors;

public interface IMenuItemService {

    Case createFilter(FilterBody body) throws TransitionNotExecutableException;
    Case updateFilter(Case filterCase, FilterBody body);
    Case createMenuItem(MenuItemBody body) throws TransitionNotExecutableException;
    Case updateMenuItem(Case itemCase, MenuItemBody body) throws TransitionNotExecutableException;
    Case createOrUpdateMenuItem(MenuItemBody body) throws TransitionNotExecutableException;
    Case createOrIgnoreMenuItem(MenuItemBody body) throws TransitionNotExecutableException;
    Case findMenuItem(String identifier);
    Case findMenuItem(String uri, String name);
    Case findFolderCase(UriNode node);
    boolean existsMenuItem(String identifier);
    void moveItem(Case item, String destUri) throws TransitionNotExecutableException;
    Case duplicateItem(Case originItem, I18nString newTitle, String newIdentifier) throws TransitionNotExecutableException;
    Case removeChildItemFromParent(String folderId, Case childItem);

    /**
     * Gets all tabbed or non-tabbed views
     *
     * @param isTabbed if true, only tabbed views will be returned
     * @param isPrimary if true, only views accessible directly from the menu_item will be returned
     *
     * @return All available views defined in {@link MenuItemView} in consideration of input value. Views are returned as
     * options for {@link MapOptionsField}
     * */
    default Map<String, I18nString> getAvailableViewsAsOptions(boolean isTabbed, boolean isPrimary) {
        return MenuItemView.findAllByIsTabbedAndIsPrimary(isTabbed, isPrimary).stream()
                .collect(Collectors.toMap(MenuItemView::getIdentifier, MenuItemView::getName));
    }

    /**
     * Gets all tabbed or non-tabbed views
     *
     * @param isTabbed if true, only tabbed views will be returned
     * @param viewIdentifier identifier of view (defined in {@link MenuItemView}), which is parent to returned views
     *
     * @return All available views defined in {@link MenuItemView} in consideration of input values. Views are returned as
     * options for {@link MapOptionsField}
     * */
    default Map<String, I18nString> getAvailableViewsAsOptions(boolean isTabbed, String viewIdentifier) {
        int index = viewIdentifier.lastIndexOf("_configuration");
        if (index > 0) {
            viewIdentifier = viewIdentifier.substring(0, index);
        }
        return MenuItemView.findAllByIsTabbedAndParentIdentifier(isTabbed, viewIdentifier).stream()
                .collect(Collectors.toMap(MenuItemView::getIdentifier, MenuItemView::getName));
    }

}
