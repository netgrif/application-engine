package com.netgrif.application.engine.settings.service;

import com.netgrif.application.engine.settings.domain.Preferences;
import org.springframework.stereotype.Service;

public interface IPreferencesService {

    Preferences get(String userId);

    Preferences save(Preferences preferences);
}