package com.netgrif.workflow.settings.web;

import com.netgrif.workflow.settings.domain.RolePreferences;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;

public class RolePreferencesResource extends Resource<RolePreferences> {

    public RolePreferencesResource(RolePreferences content) {
        super(content, new ArrayList<>());
    }
}