package com.netgrif.application.engine.menu.domain;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * todo javadoc
 * */
@Getter
public enum MenuItemView {
    // todo translations
    TABBED_CASE_VIEW(new I18nString("Tabbed case view"), "tabbed_case_view",
            List.of("tabbed_task_view"), true),
    TABBED_TASK_VIEW(new I18nString("Tabbed task view"), "tabbed_task_view", List.of(), true);

    private final I18nString name;
    private final String identifier;
    /**
     * todo javadoc
     * */
    private final List<String> allowedAssociatedViews;
    private final boolean isTabbed;

    MenuItemView(I18nString name, String identifier, List<String> allowedAssociatedViews, boolean isTabbed) {
        this.name = name;
        this.identifier = identifier;
        this.allowedAssociatedViews = allowedAssociatedViews;
        this.isTabbed = isTabbed;
    }

    /**
     * todo javadoc
     * */
    public static MenuItemView fromIdentifier(String identifier) {
        for (MenuItemView view : MenuItemView.values()) {
            if (view.identifier.equals(identifier)) {
                return view;
            }
        }
        throw new IllegalArgumentException(identifier);
    }

    /**
     * todo javadoc
     * */
    public static List<MenuItemView> findAllByIsTabbed(boolean isTabbed) {
        return Arrays.stream(MenuItemView.values())
                .filter(view -> view.isTabbed == isTabbed)
                .collect(Collectors.toList());
    }

    /**
     * todo javadoc
     * */
    public static List<MenuItemView> findAllByIsTabbedAndParentIdentifier(boolean isTabbed, String parentIdentifier) {
        MenuItemView parentView = fromIdentifier(parentIdentifier);
        return Arrays.stream(MenuItemView.values())
                .filter(view -> view.isTabbed == isTabbed
                        && parentView.getAllowedAssociatedViews().contains(view.identifier))
                .collect(Collectors.toList());
    }
}
