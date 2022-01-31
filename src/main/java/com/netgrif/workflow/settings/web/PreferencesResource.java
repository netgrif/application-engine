package com.netgrif.workflow.settings.web;

import com.netgrif.workflow.settings.domain.Preferences;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;

public class PreferencesResource extends Resource<Preferences> {

    public PreferencesResource(Preferences content) {
        super(content, new ArrayList<>());
    }
}