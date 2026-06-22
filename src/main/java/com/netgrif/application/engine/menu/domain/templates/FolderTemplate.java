package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.Map;

public class FolderTemplate implements Template {

    public static final String IDENTIFIER = "folder";

    private static final I18nString NAME = new I18nString("Folder without view",
            Map.of("sk", "Priečinok bez zobrazenia", "de", "Ordner ohne Ansicht"));

    private static MenuItemBody buildTemplate() {
        MenuItemBody menuItemBody = new MenuItemBody();
        menuItemBody.setConfigurationTemplateIdentifier(IDENTIFIER);
        menuItemBody.setUseTabbedView(false);
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
