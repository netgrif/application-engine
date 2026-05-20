package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.TaskViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class TabbedTaskViewTemplate implements Template {

    public static final String IDENTIFIER = "tabbed_task_view";

    private static final I18nString NAME = new I18nString("Tabbed task view",
            Map.of("sk", "Zobrazenie úloh v záložkách", "de", "Aufgabenansicht in Registerkarten"));

    private static final MenuItemBody TEMPLATE = buildTemplate();

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setConfigurationTemplateIdentifier(IDENTIFIER);
        menuItemBody.setUseTabbedView(true);

        TaskViewBody taskViewBody = new TaskViewBody();

        taskViewBody.setFilterBody(Template.defaultTaskFilterBody(NAME));
        menuItemBody.setView(taskViewBody);

        return menuItemBody;
    }

    public String getIdentifier() {
        return IDENTIFIER;
    }

    public I18nString getName() {
        return NAME;
    }

    public MenuItemBody getTemplate() {
        return TEMPLATE;
    }
}
