package com.netgrif.application.engine.menu.domain.templates;

import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemBody;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;

public interface Template {
    String getIdentifier();
    I18nString getName();
    MenuItemBody getTemplate();

    static FilterBody defaultTaskFilterBody() {
        FilterBody filterBody = new FilterBody();
        filterBody.setType(FieldType.TASK_FILTER);
        filterBody.setQuery("tasks");
        return filterBody;
    }

    static FilterBody defaultSingleTaskFilterBody() {
        FilterBody filterBody = new FilterBody();
        filterBody.setType(FieldType.TASK_FILTER);
        filterBody.setQuery("task");
        return filterBody;
    }

    static FilterBody defaultCaseFilterBody() {
        FilterBody filterBody = new FilterBody();
        filterBody.setType(FieldType.CASE_FILTER);
        filterBody.setQuery("cases");
        return filterBody;
    }
}
