package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.CaseViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class SimpleCaseViewTemplate implements Template {

    public static final String IDENTIFIER = "simple_case_view";

    private static final I18nString NAME = new I18nString("Simple case view",
            Map.of("sk", "", "de", "")); // todo 23 translate

    private static final MenuItemBody TEMPLATE = buildTemplate();

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setUseTabbedView(false);

        CaseViewBody caseViewBody = new CaseViewBody();
        caseViewBody.setFilterBody(Template.defaultCaseFilterBody(NAME));
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
