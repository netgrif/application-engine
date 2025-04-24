package com.netgrif.application.engine.settings.service;

import com.netgrif.application.engine.settings.domain.Preferences;

public interface IPreferencesService {

    Preferences get(String userId);

    Preferences save(Preferences preferences);
}