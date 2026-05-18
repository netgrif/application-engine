package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;

import java.util.List;

public interface Template {
    String getIdentifier();
    I18nString getName();
    MenuItemBody getTemplate();

    static FilterBody defaultTaskFilterBody(I18nString name) {
        FilterBody filterBody = new FilterBody();
        filterBody.setIcon("filter");
        filterBody.setType("Task");
        filterBody.setVisibility("private");
        filterBody.setTitle(name);
        filterBody.setQuery("*");
        filterBody.setAllowedNets(List.of());
        return filterBody;
    }

    static FilterBody defaultCaseFilterBody(I18nString name) {
        FilterBody filterBody = new FilterBody();
        filterBody.setIcon("filter");
        filterBody.setType("Case");
        filterBody.setVisibility("private");
        filterBody.setTitle(name);
        filterBody.setQuery("*");
        filterBody.setAllowedNets(List.of());
        return filterBody;
    }
}
