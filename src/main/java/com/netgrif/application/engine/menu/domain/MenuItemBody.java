package com.netgrif.application.engine.menu.domain;

import com.netgrif.application.engine.menu.domain.configurations.ViewBody;
import com.netgrif.application.engine.menu.domain.configurations.ViewConstants;
import com.netgrif.application.engine.menu.utils.MenuItemUtils;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Class, that holds configurable attributes of menu item.
 */
@Data
@NoArgsConstructor
public class MenuItemBody {
    private String uri;
    private String identifier;

    private String menuIcon = "filter_none";
    private I18nString menuName;
    private Map<String, I18nString> allowedRoles;
    private Map<String, I18nString> bannedRoles;
    private boolean useCustomView = false;
    private String customViewSelector;
    private boolean isAutoSelect = false;

    private boolean useTabbedView;
    private String tabIcon;
    private boolean useTabIcon = true;
    private I18nString tabName;
    private ViewBody view;

    public MenuItemBody(I18nString name, String icon) {
        this.menuName = name;
        this.tabName = name;
        this.menuIcon = icon;
        this.tabIcon = icon;
    }

    public MenuItemBody(I18nString menuName, I18nString tabName, String menuIcon, String tabIcon) {
        this.menuName = menuName;
        this.tabName = tabName;
        this.menuIcon = menuIcon;
        this.tabIcon = tabIcon;
    }

    public MenuItemBody(String uri, String identifier, I18nString name, String icon) {
        this.uri = uri;
        this.identifier = identifier;
        this.menuName = name;
        this.tabName = name;
        this.menuIcon = icon;
        this.tabIcon = icon;
    }

    public MenuItemBody(String uri, String identifier, I18nString menuName, I18nString tabName, String menuIcon, String tabIcon) {
        this.uri = uri;
        this.identifier = identifier;
        this.menuName = menuName;
        this.tabName = tabName;
        this.menuIcon = menuIcon;
        this.tabIcon = tabIcon;
    }

    public MenuItemBody(String uri, String identifier, String name, String icon) {
        this.uri = uri;
        this.identifier = identifier;
        this.menuName = new I18nString(name);
        this.tabName = new I18nString(name);
        this.menuIcon = icon;
        this.tabIcon = icon;
    }

    public MenuItemBody(String uri, String identifier, String menuName, String tabName, String menuIcon, String tabIcon) {
        this.uri = uri;
        this.identifier = identifier;
        this.menuName = new I18nString(menuName);
        this.tabName = new I18nString(tabName);
        this.menuIcon = menuIcon;
        this.tabIcon = tabIcon;
    }

    public String getIdentifier() {
        return MenuItemUtils.sanitize(this.identifier);
    }

    public void setMenuName(I18nString name) {
        this.menuName = name;
    }

    public void setMenuName(String name) {
        this.menuName = new I18nString(name);
    }

    public void setTabName(I18nString name) {
        this.tabName = name;
    }

    public void setTabName(String name) {
        this.tabName = new I18nString(name);
    }

    /**
     * @return true if the menu item contains view
     * */
    public boolean hasView() {
        return this.view != null;
    }

    /**
     * Transforms attributes into dataSet for {@link IDataService#setData}
     *
     * @return {@link ToDataSetOutcome} object with dataSet
     */
    public ToDataSetOutcome toDataSet() {
        return toDataSet(null, null, null);
    }

    /**
     * Transforms attributes into dataSet for {@link IDataService#setData}
     *
     * @param viewCase case instance of view. If provided, caseRef and taskRef are initialized
     *
     * @return {@link ToDataSetOutcome} object with dataSet
     */
    public ToDataSetOutcome toDataSet(Case viewCase) {
        return toDataSet(null, null, viewCase);
    }

    /**
     * Transforms attributes into dataSet for {@link IDataService#setData}
     *
     * @param parentId identifier of parent menu item instance
     * @param nodePath uri, that represents the menu item (f.e.: "/myItem1/myItem2")
     * @param viewCase case instance of view. If provided, caseRef and taskRef are initialized
     *
     * @return {@link ToDataSetOutcome} object with dataSet
     */
    public ToDataSetOutcome toDataSet(String parentId, String nodePath, Case viewCase) {
        ToDataSetOutcome outcome = new ToDataSetOutcome();

        if (parentId != null) {
            outcome.putDataSetEntry(MenuItemConstants.FIELD_PARENT_ID, FieldType.CASE_REF, List.of(parentId));
        }
        if (nodePath != null) {
            outcome.putDataSetEntry(MenuItemConstants.FIELD_NODE_PATH, FieldType.TEXT, nodePath);
        }
        outcome.putDataSetEntry(MenuItemConstants.FIELD_MENU_NAME, FieldType.I18N, this.menuName);
        outcome.putDataSetEntry(MenuItemConstants.FIELD_MENU_ICON, FieldType.TEXT, this.menuIcon);
        outcome.putDataSetEntry(MenuItemConstants.FIELD_USE_TABBED_VIEW, FieldType.BOOLEAN, this.useTabbedView);
        outcome.putDataSetEntry(MenuItemConstants.FIELD_TAB_NAME, FieldType.I18N, this.tabName);
        outcome.putDataSetEntry(MenuItemConstants.FIELD_TAB_ICON, FieldType.TEXT, this.tabIcon);
        if (this.identifier != null) {
            outcome.putDataSetEntry(MenuItemConstants.FIELD_IDENTIFIER, FieldType.TEXT, this.getIdentifier());
        }
        outcome.putDataSetEntry(MenuItemConstants.FIELD_USE_TAB_ICON, FieldType.BOOLEAN, this.useTabIcon);
        outcome.putDataSetEntry(MenuItemConstants.FIELD_USE_CUSTOM_VIEW, FieldType.BOOLEAN,
                this.useCustomView);
        outcome.putDataSetEntry(MenuItemConstants.FIELD_CUSTOM_VIEW_SELECTOR, FieldType.TEXT,
                this.customViewSelector);
        outcome.putDataSetEntry(MenuItemConstants.FIELD_IS_AUTO_SELECT, FieldType.BOOLEAN, this.isAutoSelect);
        outcome.putDataSetEntryOptions(MenuItemConstants.FIELD_ALLOWED_ROLES, FieldType.MULTICHOICE_MAP, this.allowedRoles);
        outcome.putDataSetEntryOptions(MenuItemConstants.FIELD_BANNED_ROLES, FieldType.MULTICHOICE_MAP, this.bannedRoles);

        if (viewCase != null) {
            outcome.putDataSetEntry(MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE, FieldType.ENUMERATION_MAP,
                    this.view.getViewType().getIdentifier());
            outcome.putDataSetEntry(MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID, FieldType.CASE_REF,
                    List.of(viewCase.getStringId()));
            String taskId = MenuItemUtils.findTaskIdInCase(viewCase, ViewConstants.TRANS_SETTINGS_ID);
            outcome.putDataSetEntry(MenuItemConstants.FIELD_VIEW_CONFIGURATION_FORM, FieldType.TASK_REF, List.of(taskId));
        }

        return outcome;
    }
}
