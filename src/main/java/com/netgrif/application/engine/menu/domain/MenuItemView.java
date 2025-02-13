package com.netgrif.application.engine.menu.domain;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Here is listed and configured every configuration process available for menu items.
 * */
@Getter
public enum MenuItemView {
    TABBED_CASE_VIEW(new I18nString("Tabbed case view", Map.of("sk", "Zobrazenie prípadov v taboch",
            "de", "Fallansicht mit Registerkarten")), "tabbed_case_view", List.of("tabbed_task_view"), true),
    TABBED_TASK_VIEW(new I18nString("Tabbed task view", Map.of("sk", "Zobrazenie úloh v taboch",
            "de", "Aufgabenansicht mit Registerkarten")), "tabbed_task_view", List.of(), true),
    TABBED_TICKET_VIEW(new I18nString("Tabbed ticket view", Map.of()), "tabbed_ticket_view",
            List.of("tabbed_single_task_view"), true),
    TABBED_SINGLE_TASK_VIEW(new I18nString("Tabbed single task view", Map.of()),
            "tabbed_single_task_view", List.of(), true);

    private final I18nString name;
    private final String identifier;
    /**
     * List of view identifiers of views, that can be associated with the view
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
     * Builds enum value by the view identifier
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
     * Finds all enum values, that are tabbed or non-tabbed
     *
     * @param isTabbed if true, only tabbed values will be returned
     *
     * @return List of views based on {@link #isTabbed}
     * */
    public static List<MenuItemView> findAllByIsTabbed(boolean isTabbed) {
        return Arrays.stream(MenuItemView.values())
                .filter(view -> view.isTabbed == isTabbed)
                .collect(Collectors.toList());
    }

    /**
     * Finds all enum values, that are tabbed or non-tabbed and are defined in parent view as {@link #allowedAssociatedViews}
     *
     * @param isTabbed if true, set of views is reduced to only tabbed views
     * @param parentIdentifier identifier of the view, that contains returned views in {@link #allowedAssociatedViews}
     *
     * @return List of views based on {@link #isTabbed} and {@link #allowedAssociatedViews}
     * */
    public static List<MenuItemView> findAllByIsTabbedAndParentIdentifier(boolean isTabbed, String parentIdentifier) {
        MenuItemView parentView = fromIdentifier(parentIdentifier);
        return Arrays.stream(MenuItemView.values())
                .filter(view -> view.isTabbed == isTabbed
                        && parentView.getAllowedAssociatedViews().contains(view.identifier))
                .collect(Collectors.toList());
    }
}
