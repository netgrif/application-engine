package com.netgrif.application.engine.settings.service;

import com.netgrif.application.engine.settings.domain.Preferences;

import java.util.Optional;

public interface IPreferencesService {

    Optional<Preferences> get(String identityId);

    Preferences save(Preferences preferences);
}