package com.netgrif.application.engine.menu.service;

import com.netgrif.application.engine.menu.domain.templates.*;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Holder class that manages predefined menu item templates.
 * <p>
 * This class provides a central registry for menu item templates that can be used
 * throughout the application. Templates are identified by unique string identifiers
 * and can be retrieved or transformed into user-selectable options.
 * </p>
 */
public class MenuItemTemplateHolder {
    /**
     * Map of available menu item templates indexed by their unique identifiers.
     * <p>
     * Each entry maps a template identifier to its corresponding Template instance.
     * This map is immutable and initialized with predefined templates.
     * The key is the template identifier (String), and the value is the Template instance.
     * </p>
     */
    private final static Map<String, Template> templates = Map.of(
            TabbedCaseViewTemplate.IDENTIFIER, new TabbedCaseViewTemplate(),
            TabbedTaskViewTemplate.IDENTIFIER, new TabbedTaskViewTemplate(),
            SimpleCaseViewTemplate.IDENTIFIER, new SimpleCaseViewTemplate(),
            SimpleTaskViewTemplate.IDENTIFIER, new SimpleTaskViewTemplate(),
            SingleTaskViewTemplate.IDENTIFIER, new SingleTaskViewTemplate(),
            TabbedTicketViewTemplate.IDENTIFIER, new TabbedTicketViewTemplate()
    );


    /**
     * Retrieves a template by its unique identifier.
     * <p>
     * This method looks up and returns the Template instance associated with the
     * provided identifier from the templates registry.
     * </p>
     *
     * @param identifier the unique identifier of the template to retrieve
     * @return an Optional containing the Template instance if found, or empty Optional if no template exists for the given identifier
     */
    public static Optional<Template> get(String identifier) {
        return Optional.ofNullable(templates.get(identifier));
    }

    /**
     * Transforms the available templates into a map of selectable options.
     * <p>
     * This method converts the templates map into a format suitable for displaying
     * as user-selectable options, where the key is the template identifier and the
     * value is the internationalized name of the template.
     * </p>
     *
     * @return a map where keys are template identifiers and values are internationalized template names
     */
    public static Map<String, I18nString> transformToOptions() {
        return templates.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getName()
                ));
    }
}
