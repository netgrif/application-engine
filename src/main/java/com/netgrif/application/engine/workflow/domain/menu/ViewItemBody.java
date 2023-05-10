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
public class ViewItemBody extends ItemBody {
    private String uri;
    private String identifier;
    private Case filter;
    private List<String> defaultHeaders;

    public ViewItemBody(String uri, String identifier, Case filter, List<String> defaultHeaders, Map<String, I18nString> allowedRoles, Map<String, I18nString> bannedRoles) {
        super(allowedRoles, bannedRoles);
        this.uri = uri;
        this.identifier = identifier;
        this.filter = filter;
        this.defaultHeaders = defaultHeaders;
    }
}
