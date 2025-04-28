package com.netgrif.application.engine.workflow.domain.menu;

import lombok.Getter;

/**
 * Enumeration for menu items. It contains any constants needed in application.
 */
public enum MenuItemConstants {

    // FIELDS
    PREFERENCE_ITEM_FIELD_NEW_FILTER_ID("new_filter_id"),
    PREFERENCE_ITEM_FIELD_FILTER_CASE("filter_case"),
    PREFERENCE_ITEM_FIELD_PARENT_ID("parentId"),
    PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS("childItemIds"),
    PREFERENCE_ITEM_FIELD_HAS_CHILDREN("hasChildren"),
    PREFERENCE_ITEM_FIELD_CASE_DEFAULT_HEADERS("case_default_headers"),
    PREFERENCE_ITEM_FIELD_TASK_DEFAULT_HEADERS("task_default_headers"),
    PREFERENCE_ITEM_FIELD_IDENTIFIER("menu_item_identifier"),
    PREFERENCE_ITEM_FIELD_APPEND_MENU_ITEM("append_menu_item_stringId"),
    PREFERENCE_ITEM_FIELD_ALLOWED_ROLES("allowed_roles"),
    PREFERENCE_ITEM_FIELD_BANNED_ROLES("banned_roles"),
    PREFERENCE_ITEM_FIELD_MENU_NAME("menu_name"),
    PREFERENCE_ITEM_FIELD_MENU_ICON("menu_icon"),
    PREFERENCE_ITEM_FIELD_TAB_NAME("tab_name"),
    PREFERENCE_ITEM_FIELD_USE_TAB_ICON("use_tab_icon"),
    PREFERENCE_ITEM_FIELD_TAB_ICON("tab_icon"),
    PREFERENCE_ITEM_FIELD_NODE_PATH("nodePath"),
    PREFERENCE_ITEM_FIELD_NODE_NAME("nodeName"),
    PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE("duplicate_new_title"),
    PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER("duplicate_view_identifier"),
    PREFERENCE_ITEM_FIELD_DUPLICATE_RESET_CHILD_ITEM_IDS("duplicate_reset_childItemIds"),
    PREFERENCE_ITEM_FIELD_REQUIRE_TITLE_IN_CREATION("case_require_title_in_creation"),
    PREFERENCE_ITEM_FIELD_USE_CUSTOM_VIEW("use_custom_view"),
    PREFERENCE_ITEM_FIELD_CUSTOM_VIEW_SELECTOR("custom_view_selector"),
    PREFERENCE_ITEM_FIELD_CASE_VIEW_SEARCH_TYPE("case_view_search_type"),
    PREFERENCE_ITEM_FIELD_CREATE_CASE_BUTTON_TITLE("create_case_button_title"),
    PREFERENCE_ITEM_FIELD_CREATE_CASE_BUTTON_ICON("create_case_button_icon"),
    PREFERENCE_ITEM_FIELD_BANNED_NETS_IN_CREATION("case_banned_nets_in_creation"),
    PREFERENCE_ITEM_FIELD_SHOW_CREATE_CASE_BUTTON("show_create_case_button"),
    PREFERENCE_ITEM_FIELD_CASE_SHOW_MORE_MENU("case_show_more_menu"),
    PREFERENCE_ITEM_FIELD_CASE_ALLOW_HEADER_TABLE_MODE("case_allow_header_table_mode"),
    PREFERENCE_ITEM_FIELD_CASE_HEADERS_MODE("case_headers_mode"),
    PREFERENCE_ITEM_FIELD_CASE_HEADERS_DEFAULT_MODE("case_headers_default_mode"),
    PREFERENCE_ITEM_FIELD_CASE_IS_HEADER_MODE_CHANGEABLE("case_is_header_mode_changeable"),
    PREFERENCE_ITEM_FIELD_USE_CASE_DEFAULT_HEADERS("case_is_header_mode_changeable"),
    PREFERENCE_ITEM_FIELD_ADDITIONAL_FILTER_CASE("additional_filter_case"),
    PREFERENCE_ITEM_FIELD_MERGE_FILTERS("merge_filters"),
    PREFERENCE_ITEM_FIELD_TASK_VIEW_SEARCH_TYPE("task_view_search_type"),
    PREFERENCE_ITEM_FIELD_TASK_HEADERS_MODE("task_headers_mode"),
    PREFERENCE_ITEM_FIELD_TASK_HEADERS_DEFAULT_MODE("task_headers_default_mode"),
    PREFERENCE_ITEM_FIELD_TASK_IS_HEADER_MODE_CHANGEABLE("task_is_header_mode_changeable"),
    PREFERENCE_ITEM_FIELD_TASK_ALLOW_HEADER_TABLE_MODE("task_allow_header_table_mode"),
    PREFERENCE_ITEM_FIELD_USE_TASK_DEFAULT_HEADERS("use_task_default_headers"),
    PREFERENCE_ITEM_FIELD_TASK_SHOW_MORE_MENU("task_show_more_menu"),

    // TRANSITIONS
    PREFERENCE_ITEM_SETTINGS_TRANS_ID("item_settings"),
    PREFERENCE_ITEM_FIELD_INIT_TRANS_ID("initialize");
    @Getter
    private final String attributeId;

    MenuItemConstants(String attributeId) {
        this.attributeId = attributeId;
    }
}
