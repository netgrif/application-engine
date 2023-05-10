package com.netgrif.application.engine.workflow.domain.menu;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderItemBody extends ItemBody {
    private I18nString name;

    public FolderItemBody(I18nString name, Map<String, I18nString> allowedRoles, Map<String, I18nString> bannedRoles) {
        super(allowedRoles, bannedRoles);
        this.name = name;
    }
}
