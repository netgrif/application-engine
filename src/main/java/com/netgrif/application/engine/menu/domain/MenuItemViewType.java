package com.netgrif.application.engine.menu.domain;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.menu.domain.ViewType.*;


/**
 * Here is listed and described every configuration process available for menu items.
 * */
@Getter
public enum MenuItemViewType {
    CASE_VIEW(new I18nString("Case view",
            Map.of("sk", "Zobrazenie prípadov", "de", "Fallansicht")),
            "case_view", List.of("task_view"), TABBED_AND_UNTABBED, true),
    TASK_VIEW(new I18nString("Task view",
            Map.of("sk", "Zobrazenie úloh", "de", "Aufgabenansicht")),
            "task_view", List.of(), TABBED_AND_UNTABBED, true),
    TABBED_TICKET_VIEW(new I18nString("Tabbed ticket view",
            Map.of("sk", "Tiketové zobrazenie v taboch", "de", "Ticketansicht mit Registerkarten")),
            "tabbed_ticket_view", List.of("single_task_view"), ONLY_TABBED, true),
    SINGLE_TASK_VIEW(new I18nString("Single task view",
            Map.of("sk", "Zobrazenie jednej úlohy", "de", "Einzelaufgabenansicht")),
            "single_task_view", List.of(), TABBED_AND_UNTABBED, true);

    private final I18nString name;
    private final String identifier;

    /**
     * List of view identifiers of views, that can be associated with the view
     * */
    private final List<String> allowedAssociatedViews;

    /**
     * Specifies whether this view type can be used in tabbed menu items, untabbed menu items, or both.
     * This determines the context in which the view can be displayed in the menu structure.
     * */
    private final ViewType viewType;

    /**
     * if false, the view cannot be used as first configuration of the menu_item, but can be used as secondary
     * (associated to another view)
     * */
    private final boolean isPrimary;

    MenuItemViewType(I18nString name, String identifier, List<String> allowedAssociatedViews, ViewType viewType, boolean isPrimary) {
        this.name = name;
        this.identifier = identifier;
        this.allowedAssociatedViews = allowedAssociatedViews;
        this.viewType = viewType;
        this.isPrimary = isPrimary;
    }

    /**
     * Builds enum value by the view identifier
     * */
    public static MenuItemViewType fromIdentifier(String identifier) {
        for (MenuItemViewType view : MenuItemViewType.values()) {
            if (view.identifier.equals(identifier)) {
                return view;
            }
        }
        throw new IllegalArgumentException(identifier);
    }

    /**
     * Finds all enum values, that are / are not primary
     *
     * @param isPrimary if true, only views accessible directly from the menu_item will be returned
     *
     * @return List of views based on {@link #isPrimary}
     * */
    public static List<MenuItemViewType> findAllByIsPrimary(boolean isPrimary) {
        return Arrays.stream(MenuItemViewType.values())
                .filter(view -> view.isPrimary == isPrimary)
                .collect(Collectors.toList());
    }

    /**
     * Finds all enum values, that are are defined in parent view as {@link #allowedAssociatedViews}
     *
     * @param parentIdentifier identifier of the view, that contains returned views in {@link #allowedAssociatedViews}
     *
     * @return List of views based on {@link #allowedAssociatedViews}
     * */
    public static List<MenuItemViewType> findAllByParentIdentifier(String parentIdentifier) {
        MenuItemViewType parentView = fromIdentifier(parentIdentifier);
        return Arrays.stream(MenuItemViewType.values())
                .filter(view -> parentView.getAllowedAssociatedViews().contains(view.identifier))
                .collect(Collectors.toList());
    }
}
