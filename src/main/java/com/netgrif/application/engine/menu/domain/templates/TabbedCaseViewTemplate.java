package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.CaseViewBody;
import com.netgrif.application.engine.menu.domain.configurations.TaskViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class TabbedCaseViewTemplate implements Template {

    public static final String IDENTIFIER = "tabbed_case_view";

    private static final I18nString NAME = new I18nString("Tabbed case view",
            Map.of("sk", "", "de", "")); // todo 23 translate

    private static final MenuItemBody TEMPLATE = buildTemplate();

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        // todo 23 menu item body data
        // is tabbed

        CaseViewBody caseViewBody = new CaseViewBody();
        // todo 23 case view body data

        FilterBody filterBody = new FilterBody();
        // todo 23 filter body data

        TaskViewBody taskViewBody = new TaskViewBody();
        // todo 23 task view body data

        caseViewBody.setFilterBody(filterBody);
        caseViewBody.setChainedView(taskViewBody);
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
