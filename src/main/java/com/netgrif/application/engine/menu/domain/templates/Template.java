package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

public interface Template {
    String getIdentifier();
    I18nString getName();
    MenuItemBody getTemplate();
}
