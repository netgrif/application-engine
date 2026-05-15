package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.SingleTaskViewBody;
import com.netgrif.application.engine.menu.domain.configurations.TaskViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class SingleTaskViewTemplate implements Template {

    public static final String IDENTIFIER = "single_task_view";

    private static final I18nString NAME = new I18nString("Single task view",
            Map.of("sk", "", "de", "")); // todo 23 translate

    private static final MenuItemBody TEMPLATE = buildTemplate();

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        // todo 23 menu item body data
        // is tabbed

        SingleTaskViewBody singleTaskViewBody = new SingleTaskViewBody();
        // todo 23 task view body data

        FilterBody filterBody = new FilterBody();
        // todo 23 filter body data

        singleTaskViewBody.setFilterBody(filterBody);
        menuItemBody.setView(singleTaskViewBody);

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
