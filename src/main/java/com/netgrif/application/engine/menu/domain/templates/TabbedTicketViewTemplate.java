package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.SingleTaskViewBody;
import com.netgrif.application.engine.menu.domain.configurations.TabbedTicketViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class TabbedTicketViewTemplate implements Template {

    public static final String IDENTIFIER = "tabbed_ticket_view";

    private static final I18nString NAME = new I18nString("Tabbed ticket view",
            Map.of("sk", "Tiketové zobrazenie", "de", "Ticketansicht"));

    private static final MenuItemBody TEMPLATE = buildTemplate();

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setUseTabbedView(true);

        TabbedTicketViewBody tabbedTicketViewBody = new TabbedTicketViewBody();

        SingleTaskViewBody singleTaskViewBody = new SingleTaskViewBody();
        singleTaskViewBody.setFilterBody(Template.defaultTaskFilterBody(NAME));
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
