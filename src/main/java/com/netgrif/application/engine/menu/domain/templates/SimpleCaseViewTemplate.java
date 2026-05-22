package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.CaseViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class SimpleCaseViewTemplate implements Template {

    public static final String IDENTIFIER = "simple_case_view";

    private static final I18nString NAME = new I18nString("Simple case view",
            Map.of("sk", "Zobrazenie prípadov", "de", "Fallansicht"));

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setConfigurationTemplateIdentifier(IDENTIFIER);
        menuItemBody.setUseTabbedView(false);

        CaseViewBody caseViewBody = new CaseViewBody();
        caseViewBody.setFilterBody(Template.defaultCaseFilterBody());
        menuItemBody.setView(caseViewBody);

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
