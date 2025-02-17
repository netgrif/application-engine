package com.netgrif.application.engine.startup

import com.netgrif.application.engine.menu.domain.MenuItemView
import com.netgrif.application.engine.menu.registry.interfaces.IMenuItemViewRegistry
import com.netgrif.application.engine.petrinet.domain.I18nString
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class MenuItemViewRegistryRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IMenuItemViewRegistry registry

    public static final String TABBED_CASE_VIEW_ID = "tabbed_case_view"
    public static final String TABBED_TASK_VIEW_ID = "tabbed_task_view"
    public static final String TABBED_TICKET_VIEW_ID = "tabbed_ticket_view"
    public static final String TABBED_SINGLE_TASK_VIEW_ID = "tabbed_single_task_view"

    @Override
    void run(String... args) throws Exception {
        registerTabbedCaseView()
        registerTabbedTaskView()
        registerTabbedTicketView()
        registerTabbedSingleTaskView()
    }

    private void registerTabbedCaseView() {
        MenuItemView view = MenuItemView.with()
                .name(new I18nString("Tabbed case view", Map.of("sk", "Zobrazenie prípadov v taboch",
                        "de", "Fallansicht mit Registerkarten")))
                .identifier(TABBED_CASE_VIEW_ID)
                .allowedAssociatedViews(List.of(TABBED_TASK_VIEW_ID))
                .isTabbed(true)
                .isPrimary(true)
                .build()
        registry.registerView(view)
    }

    private void registerTabbedTaskView() {
        MenuItemView view = MenuItemView.with()
                .name(new I18nString("Tabbed task view", Map.of("sk", "Zobrazenie úloh v taboch",
                        "de", "Aufgabenansicht mit Registerkarten")))
                .identifier(TABBED_TASK_VIEW_ID)
                .allowedAssociatedViews(List.of())
                .isTabbed(true)
                .isPrimary(true)
                .build()
        registry.registerView(view)
    }

    private void registerTabbedTicketView() {
        MenuItemView view = MenuItemView.with()
                .name(new I18nString("Tabbed ticket view", Map.of("sk", "Tiketové zobrazenie v taboch",
                        "de", "Ticketansicht mit Registerkarten")))
                .identifier(TABBED_TICKET_VIEW_ID)
                .allowedAssociatedViews(List.of(TABBED_SINGLE_TASK_VIEW_ID))
                .isTabbed(true)
                .isPrimary(true)
                .build()
        registry.registerView(view)
    }

    private void registerTabbedSingleTaskView() {
        MenuItemView view = MenuItemView.with()
                .name(new I18nString("Tabbed single task view", Map.of("sk", "Zobrazenie jednej úlohy v taboch",
                        "de", "Einzelaufgabenansicht mit Registerkarten")))
                .identifier(TABBED_SINGLE_TASK_VIEW_ID)
                .allowedAssociatedViews(List.of())
                .isTabbed(true)
                .isPrimary(false)
                .build()
        registry.registerView(view)
    }
}
