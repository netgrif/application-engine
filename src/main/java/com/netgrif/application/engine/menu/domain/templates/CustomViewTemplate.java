package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.menu.domain.configurations.SingleTaskViewBody;
import com.netgrif.application.engine.menu.domain.configurations.TabbedTicketViewBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class CustomViewTemplate implements Template {
    public static final String IDENTIFIER = "none";

    private static final I18nString NAME = new I18nString("Custom view",
            Map.of("sk", "Vlastné zobrazenie", "de", "Benutzerdefinierte Ansicht"));

    private static final MenuItemBody TEMPLATE = buildTemplate();

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setUseTabbedView(false);
        menuItemBody.setUseCustomView(true);

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
