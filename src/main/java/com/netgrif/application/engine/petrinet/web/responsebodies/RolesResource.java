package com.netgrif.application.engine.petrinet.web.responsebodies;


import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RolesResource extends EntityModel<RolesAndPermissions> {

    public RolesResource(RolesAndPermissions content) {
        super(content, new ArrayList<>());
    }

    public RolesResource(List<ProcessRole> content, Map<String, Map<CasePermission, Boolean>> permissions, Locale locale) {
        this(new RolesAndPermissions(content, permissions));
    }
}