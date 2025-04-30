package com.netgrif.application.engine.settings.web;

import com.netgrif.application.engine.settings.domain.Preferences;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;

public class PreferencesResource extends EntityModel<Preferences> {

    public PreferencesResource(Preferences content) {
        super(content, new ArrayList<>());
    }
}