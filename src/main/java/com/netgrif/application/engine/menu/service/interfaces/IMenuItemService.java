package com.netgrif.application.engine.menu.service.interfaces;

import com.netgrif.application.engine.menu.domain.ConfigurationTemplateOutcome;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.MenuItemViewType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface IMenuItemService {

    void ensureDatabaseIndexes();
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
    ConfigurationTemplateOutcome handleConfigurationTemplate(Case menuItemCase) throws TransitionNotExecutableException;
    Map<String, I18nString> collectRoles(List<ProcessRole> roles);
    Map<String, I18nString> collectRoles(Map<String, String> roles);

    /**
     * Gets all primary views as options
     *
     * @return All available views defined in {@link MenuItemViewType} in consideration of input value. Views are returned as
     * options for {@link MapOptionsField}
     * */
    default Map<String, I18nString> getPrimaryViewsAsOptions() {
        return MenuItemViewType.findAllByIsPrimary(true).stream()
                .collect(Collectors.toMap(MenuItemViewType::getIdentifier, MenuItemViewType::getName));
    }

    /**
     * Gets all views available for the configuration identifier
     *
     * @param viewIdentifier identifier of view (defined in {@link MenuItemViewType}), which is parent to returned views
     *
     * @return All available views defined in {@link MenuItemViewType} in consideration of input values. Views are returned as
     * options for {@link MapOptionsField}
     * */
    default Map<String, I18nString> getAvailableViewsAsOptions(String viewIdentifier) {
        int index = viewIdentifier.lastIndexOf("_configuration");
        if (index > 0) {
            viewIdentifier = viewIdentifier.substring(0, index);
        }
        return MenuItemViewType.findAllByParentIdentifier(viewIdentifier).stream()
                .collect(Collectors.toMap(MenuItemViewType::getIdentifier, MenuItemViewType::getName));
    }

}
