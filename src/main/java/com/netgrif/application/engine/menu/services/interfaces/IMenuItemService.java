package com.netgrif.application.engine.menu.services.interfaces;

import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.workflow.domain.Case;

import java.util.Map;

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
    Map<String, I18nString> getAvailableViewsAsOptions(boolean isTabbed, boolean isPrimary);
    Map<String, I18nString> getAvailableViewsAsOptions(boolean isTabbed, String viewIdentifier);
}
