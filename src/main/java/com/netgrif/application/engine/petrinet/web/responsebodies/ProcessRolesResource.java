package com.netgrif.application.engine.petrinet.web.responsebodies;


import com.netgrif.application.engine.petrinet.domain.roles.CasePermission;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProcessRolesResource extends EntityModel<ProcessRolesAndPermissions> {

    public ProcessRolesResource(ProcessRolesAndPermissions content) {
        super(content, new ArrayList<>());
    }

    public ProcessRolesResource(List<ProcessRole> content, Map<String, Map<CasePermission, Boolean>> permissions, Locale locale) {
        this(new ProcessRolesAndPermissions(content, permissions));
    }
}