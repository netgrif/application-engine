package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.SingleTaskViewBody;
import com.netgrif.application.engine.menu.domain.configurations.TabbedTicketViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class TabbedTicketViewTemplate implements Template {

    public static final String IDENTIFIER = "tabbed_ticket_view";

    private static final I18nString NAME = new I18nString("Tabbed ticket view",
            Map.of("sk", "", "de", "")); // todo 23 translate

    private static final MenuItemBody TEMPLATE = buildTemplate();

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        // todo 23 menu item body data
        // is tabbed

        TabbedTicketViewBody tabbedTicketViewBody = new TabbedTicketViewBody();
        // todo 23 case view body data

        SingleTaskViewBody singleTaskViewBody = new SingleTaskViewBody();
        // todo 23 task view body data

        tabbedTicketViewBody.setChainedView(singleTaskViewBody);
        menuItemBody.setView(tabbedTicketViewBody);

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
