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
public class MenuItemBody {

    // generic attributes
    private I18nString name;
    private String icon;
    private String uri;
    private String identifier;
    private Case filter;
    private Map<String, I18nString> allowedRoles;
    private Map<String, I18nString> bannedRoles;
    private boolean useTabIcon;
    private boolean useCustomView;
    private String customViewSelector;

    // case view attributes
    private String caseViewSearchType;
    private String createCaseButtonTitle;
    private String createCaseButtonIcon;
    private boolean caseRequireTitleInCreation;
    private String bannedNetsInCreation;
    private boolean caseShowMoreMenu;
    private boolean caseAllowHeaderTableMode;
    private List<String> caseHeadersMode;
    private String caseHeadersDefaultMode;
    private List<String> caseDefaultHeaders;
    private boolean caseIsHeaderModeChangeable;
    private boolean caseUseDefaultHeaders;

    // task view attributes
    private Case additionalFilter;
    private boolean mergeFilters;
    private String taskViewSearchType;
    private List<String> taskHeadersMode;
    private String taskHeadersDefaultMode;
    private boolean taskIsHeaderModeChangeable;
    private boolean taskAllowHeaderTableMode;
    private boolean taskUseDefaultHeaders;
    private List<String> taskDefaultHeaders;
    private boolean taskShowMoreMenu;

    public MenuItemBody(I18nString name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public MenuItemBody(String uri, String identifier, I18nString name, String icon) {
        this.uri = uri;
        this.identifier = identifier;
        this.name = name;
        this.icon = icon;
    }

    public MenuItemBody(String uri, String identifier, String name, String icon) {
        this.uri = uri;
        this.identifier = identifier;
        this.name = new I18nString(name);
        this.icon = icon;
    }
}
