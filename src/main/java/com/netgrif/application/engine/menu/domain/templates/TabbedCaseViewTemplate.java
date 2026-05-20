package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.CaseViewBody;
import com.netgrif.application.engine.menu.domain.configurations.TaskViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class TabbedCaseViewTemplate implements Template {

    public static final String IDENTIFIER = "tabbed_case_view";

    private static final I18nString NAME = new I18nString("Tabbed case view",
            Map.of("sk", "Zobrazenie prípadov v záložkách", "de", "Registerkartenansicht für Fälle"));

    private static final MenuItemBody TEMPLATE = buildTemplate();

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setConfigurationTemplateIdentifier(IDENTIFIER);
        menuItemBody.setUseTabbedView(true);

        CaseViewBody caseViewBody = new CaseViewBody();

        caseViewBody.setFilterBody(Template.defaultCaseFilterBody(NAME));
        caseViewBody.setChainedView(new TaskViewBody());
        menuItemBody.setView(caseViewBody);

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
