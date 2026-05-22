package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class CustomViewTemplate implements Template {
    public static final String IDENTIFIER = "custom_view";

    private static final I18nString NAME = new I18nString("Custom view",
            Map.of("sk", "Vlastné zobrazenie", "de", "Benutzerdefinierte Ansicht"));

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setConfigurationTemplateIdentifier(IDENTIFIER);
        menuItemBody.setUseTabbedView(false);
        menuItemBody.setUseCustomView(true);

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
