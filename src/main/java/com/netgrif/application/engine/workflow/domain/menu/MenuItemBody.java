package com.netgrif.application.engine.workflow.domain.menu;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.*;

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

    private static <T> void putDataSetEntry(DataSet dataSet, MenuItemConstants fieldId, Field<T> field, @Nullable T fieldValue) {
        field.setRawValue(fieldValue);
        dataSet.put(fieldId.getAttributeId(), field);
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

    /**
     * Transforms attributes into dataSet for {@link ActionDelegate#setData}
     *
     * @return created dataSet from attributes
     */
    public DataSet toDataSet() {
        return toDataSet(null, null, true);
    }

    /**
     * Transforms attributes into dataSet for {@link ActionDelegate#setData}
     *
     * @param parentId id of parent menu item instance
     * @param nodePath uri, that represents the menu item (f.e.: "/myItem1/myItem2")
     * @return created dataSet from attributes
     */
    public DataSet toDataSet(String parentId, String nodePath) {
        return toDataSet(parentId, nodePath, false);
    }

    private DataSet toDataSet(String parentId, String nodePath, boolean ignoreParentId) {
        DataSet dataSet = new DataSet();

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
            putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH, new TextField(), nodePath);
        }
        if (!ignoreParentId) {
            putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID, new CaseField(), parentIdCaseRef);
        }
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_NAME, new I18nField(), this.menuName);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_ICON, new TextField(), this.menuIcon);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TAB_NAME, new I18nField(), this.tabName);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TAB_ICON, new TextField(), this.tabIcon);
        if (this.identifier != null) {
            putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_IDENTIFIER, new TextField(), this.getIdentifier());
        }
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_FILTER_CASE, new CaseField(), filterIdCaseRefValue);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_USE_TAB_ICON, new BooleanField(), this.useTabIcon);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_USE_CUSTOM_VIEW, new BooleanField(),
                this.useCustomView);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CUSTOM_VIEW_SELECTOR, new TextField(),
                this.customViewSelector);

        // CASE
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_VIEW_SEARCH_TYPE, new EnumerationMapField(),
                this.caseViewSearchType);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CREATE_CASE_BUTTON_TITLE, new TextField(),
                this.createCaseButtonTitle);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CREATE_CASE_BUTTON_ICON, new TextField(),
                this.createCaseButtonIcon);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_REQUIRE_TITLE_IN_CREATION, new BooleanField(),
                this.caseRequireTitleInCreation);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_SHOW_CREATE_CASE_BUTTON, new BooleanField(),
                this.showCreateCaseButton);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_BANNED_NETS_IN_CREATION, new TextField(),
                this.bannedNetsInCreation);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_SHOW_MORE_MENU, new BooleanField(),
                this.caseShowMoreMenu);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_ALLOW_HEADER_TABLE_MODE, new BooleanField(),
                this.caseAllowHeaderTableMode);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_HEADERS_MODE, new MultichoiceMapField(),
                this.caseHeadersMode == null ? new HashSet<>() : new HashSet<>(this.caseHeadersMode));
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_HEADERS_DEFAULT_MODE, new EnumerationMapField(),
                this.caseHeadersDefaultMode);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_DEFAULT_HEADERS, new TextField(),
                this.caseDefaultHeaders != null ? String.join(",", this.caseDefaultHeaders) : null);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_IS_HEADER_MODE_CHANGEABLE, new BooleanField(),
                this.caseIsHeaderModeChangeable);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_USE_CASE_DEFAULT_HEADERS, new BooleanField(),
                this.caseUseDefaultHeaders);

        // TASK
        ArrayList<String> additionalFilterIdCaseRefValue = new ArrayList<>();
        if (this.additionalFilter != null) {
            additionalFilterIdCaseRefValue.add(this.additionalFilter.getStringId());
        }

        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_ADDITIONAL_FILTER_CASE, new CaseField(),
                additionalFilterIdCaseRefValue);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_MERGE_FILTERS, new BooleanField(),
                this.mergeFilters);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_VIEW_SEARCH_TYPE, new EnumerationMapField(),
                this.taskViewSearchType);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_HEADERS_MODE, new MultichoiceMapField(),
                this.taskHeadersMode == null ? new HashSet<>() : new HashSet<>(this.taskHeadersMode));
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_HEADERS_DEFAULT_MODE, new EnumerationMapField(),
                this.taskHeadersDefaultMode);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_IS_HEADER_MODE_CHANGEABLE, new BooleanField(),
                this.taskIsHeaderModeChangeable);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_ALLOW_HEADER_TABLE_MODE, new BooleanField(),
                this.taskAllowHeaderTableMode);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_USE_TASK_DEFAULT_HEADERS, new BooleanField(),
                this.taskUseDefaultHeaders);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_DEFAULT_HEADERS, new TextField(),
                this.taskDefaultHeaders != null ? String.join(",", this.taskDefaultHeaders) : null);
        putDataSetEntry(dataSet, MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_SHOW_MORE_MENU, new BooleanField(),
                this.taskShowMoreMenu);

        return dataSet;
    }
}
