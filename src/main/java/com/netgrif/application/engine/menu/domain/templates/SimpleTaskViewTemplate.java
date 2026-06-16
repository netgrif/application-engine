package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.TaskViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class SimpleTaskViewTemplate implements Template {

    public static final String IDENTIFIER = "simple_task_view";

    private static final I18nString NAME = new I18nString("Simple task view",
            Map.of("sk", "Zobrazenie úloh", "de", "Aufgabenansicht"));

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setConfigurationTemplateIdentifier(IDENTIFIER);
        menuItemBody.setUseTabbedView(false);

        TaskViewBody taskViewBody = new TaskViewBody();
        taskViewBody.setFilterBody(Template.defaultTaskFilterBody());
        menuItemBody.setView(taskViewBody);

        return menuItemBody;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public I18nString getName() {
        return NAME;
    }

    @Override
    public MenuItemBody getTemplate() {
        return buildTemplate();
    }
}
