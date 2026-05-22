package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.SingleTaskViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class SingleTaskViewTemplate implements Template {

    public static final String IDENTIFIER = "single_task_view";

    private static final I18nString NAME = new I18nString("Single task view",
            Map.of("sk", "Zobrazenie jednej úlohy", "de", "Anzeige einer Aufgabe"));

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setConfigurationTemplateIdentifier(IDENTIFIER);
        menuItemBody.setUseTabbedView(false);

        SingleTaskViewBody singleTaskViewBody = new SingleTaskViewBody();
        singleTaskViewBody.setFilterBody(Template.defaultTaskFilterBody());
        menuItemBody.setView(singleTaskViewBody);

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
