package com.netgrif.application.engine.workflow.domain.menu;

import com.netgrif.core.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate;
import com.netgrif.adapter.workflow.domain.Case;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.annotation.Nullable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class, that holds configurable attributes of menu item. In case of attribute addition, please update also
 * {@link MenuItemBody#toDataSet(String, String, boolean)} method.
 */
@Data
@NoArgsConstructor
public class MenuItemBody {

    // generic attributes
    private I18nString menuName;
    private I18nString tabName;
    private String menuIcon = "filter_none";
    private String tabIcon;
    private String uri;
    private String identifier;
    private Case filter;
    private Map<String, I18nString> allowedRoles;
    private Map<String, I18nString> bannedRoles;
    private boolean useTabIcon = true;
    private boolean useCustomView = false;
    private String customViewSelector;

    // case view attributes
    private String caseViewSearchType = "fulltext_advanced";
    private String createCaseButtonTitle;
    private String createCaseButtonIcon = "add";
    private boolean caseRequireTitleInCreation = true;
    private boolean showCreateCaseButton = true;
    private String bannedNetsInCreation;
    private boolean caseShowMoreMenu = false;
    private boolean caseAllowHeaderTableMode = true;
    private List<String> caseHeadersMode = new ArrayList<>(List.of("sort", "edit", "search"));
    private String caseHeadersDefaultMode = "sort";
    private List<String> caseDefaultHeaders;
    private boolean caseIsHeaderModeChangeable = true;
    private boolean caseUseDefaultHeaders = true;

    // task view attributes
    private Case additionalFilter;
    private boolean mergeFilters = true;
    private String taskViewSearchType = "fulltext_advanced";
    private List<String> taskHeadersMode = new ArrayList<>(List.of("sort", "edit"));
    private String taskHeadersDefaultMode = "sort";
    private boolean taskIsHeaderModeChangeable = true;
    private boolean taskAllowHeaderTableMode = true;
    private boolean taskUseDefaultHeaders = true;
    private List<String> taskDefaultHeaders;
    private boolean taskShowMoreMenu = true;

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

    private static void putDataSetEntry(Map<String, Map<String, Object>> dataSet, MenuItemConstants fieldId, FieldType fieldType,
                                        @Nullable Object fieldValue) {
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("type", fieldType.getName());
        fieldMap.put("value", fieldValue);
        dataSet.put(fieldId.getAttributeId(), fieldMap);
    }

    private static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[\\W-]+", "-")
                .toLowerCase();
    }

    public String getIdentifier() {
        return sanitize(this.identifier);
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
     * Transforms attributes into dataSet for {@link ActionDelegate#setData}
     *
     * @return created dataSet from attributes
     */
    public Map<String, Map<String, Object>> toDataSet() {
        return toDataSet(null, null, true);
    }

    /**
     * Transforms attributes into dataSet for {@link ActionDelegate#setData}
     *
     * @param parentId id of parent menu item instance
     * @param nodePath uri, that represents the menu item (f.e.: "/myItem1/myItem2")
     * @return created dataSet from attributes
     */
    public Map<String, Map<String, Object>> toDataSet(String parentId, String nodePath) {
        return toDataSet(parentId, nodePath, false);
    }

    private Map<String, Map<String, Object>> toDataSet(String parentId, String nodePath, boolean ignoreParentId) {
        Map<String, Map<String, Object>> dataSet = new LinkedHashMap<>();

        // GENERIC
        ArrayList<String> filterIdCaseRefValue = new ArrayList<>();
        if (this.filter != null) {
            filterIdCaseRefValue.add(this.filter.getStringId());
        }
        ArrayList<String> parentIdCaseRef = new ArrayList<>();
        if (parentId != null) {
            parentIdCaseRef.add(parentId);
        }

        if (nodePath != null) {
            putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH, FieldType.TEXT, nodePath);
        }
        if (!ignoreParentId) {
            putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID, FieldType.CASE_REF, parentIdCaseRef);
        }
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_NAME, FieldType.I18N, this.menuName);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_ICON, FieldType.TEXT, this.menuIcon);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TAB_NAME, FieldType.I18N, this.tabName);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TAB_ICON, FieldType.TEXT, this.tabIcon);
        if (this.identifier != null) {
            putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_IDENTIFIER, FieldType.TEXT, this.getIdentifier());
        }
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_FILTER_CASE, FieldType.CASE_REF, filterIdCaseRefValue);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_USE_TAB_ICON, FieldType.BOOLEAN, this.useTabIcon);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_USE_CUSTOM_VIEW, FieldType.BOOLEAN,
                this.useCustomView);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CUSTOM_VIEW_SELECTOR, FieldType.TEXT,
                this.customViewSelector);

        // CASE
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_VIEW_SEARCH_TYPE, FieldType.ENUMERATION_MAP,
                this.caseViewSearchType);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CREATE_CASE_BUTTON_TITLE, FieldType.TEXT,
                this.createCaseButtonTitle);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CREATE_CASE_BUTTON_ICON, FieldType.TEXT,
                this.createCaseButtonIcon);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_REQUIRE_TITLE_IN_CREATION, FieldType.BOOLEAN,
                this.caseRequireTitleInCreation);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_SHOW_CREATE_CASE_BUTTON, FieldType.BOOLEAN,
                this.showCreateCaseButton);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_BANNED_NETS_IN_CREATION, FieldType.TEXT,
                this.bannedNetsInCreation);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_SHOW_MORE_MENU, FieldType.BOOLEAN,
                this.caseShowMoreMenu);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_ALLOW_HEADER_TABLE_MODE, FieldType.BOOLEAN,
                this.caseAllowHeaderTableMode);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_HEADERS_MODE, FieldType.MULTICHOICE_MAP,
                this.caseHeadersMode == null ? new ArrayList<>() : this.caseHeadersMode);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_HEADERS_DEFAULT_MODE, FieldType.ENUMERATION_MAP,
                this.caseHeadersDefaultMode);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_DEFAULT_HEADERS, FieldType.TEXT,
                this.caseDefaultHeaders != null ? String.join(",", this.caseDefaultHeaders) : null);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_IS_HEADER_MODE_CHANGEABLE, FieldType.BOOLEAN,
                this.caseIsHeaderModeChangeable);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_USE_CASE_DEFAULT_HEADERS, FieldType.BOOLEAN,
                this.caseUseDefaultHeaders);

        // TASK
        ArrayList<String> additionalFilterIdCaseRefValue = new ArrayList<>();
        if (this.additionalFilter != null) {
            additionalFilterIdCaseRefValue.add(this.additionalFilter.getStringId());
        }

        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_ADDITIONAL_FILTER_CASE, FieldType.CASE_REF,
                additionalFilterIdCaseRefValue);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_MERGE_FILTERS, FieldType.BOOLEAN,
                this.mergeFilters);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_VIEW_SEARCH_TYPE, FieldType.ENUMERATION_MAP,
                this.taskViewSearchType);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_HEADERS_MODE, FieldType.MULTICHOICE_MAP,
                this.taskHeadersMode == null ? new ArrayList<>() : this.taskHeadersMode);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_HEADERS_DEFAULT_MODE, FieldType.ENUMERATION_MAP,
                this.taskHeadersDefaultMode);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_IS_HEADER_MODE_CHANGEABLE, FieldType.BOOLEAN,
                this.taskIsHeaderModeChangeable);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_ALLOW_HEADER_TABLE_MODE, FieldType.BOOLEAN,
                this.taskAllowHeaderTableMode);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_USE_TASK_DEFAULT_HEADERS, FieldType.BOOLEAN,
                this.taskUseDefaultHeaders);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_DEFAULT_HEADERS, FieldType.TEXT,
                this.taskDefaultHeaders != null ? String.join(",", this.taskDefaultHeaders) : null);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_SHOW_MORE_MENU, FieldType.BOOLEAN,
                this.taskShowMoreMenu);

        return dataSet;
    }
}
