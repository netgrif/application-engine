package com.netgrif.application.engine.workflow.domain.menu;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemBody {
    private I18nString name;
    private String icon;
    private String uri;
    private String identifier;
    private Case filter;
    private List<String> defaultHeaders;
    private Map<String, I18nString> allowedRoles;
    private Map<String, I18nString> bannedRoles;

    public MenuItemBody(I18nString name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}
