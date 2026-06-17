package com.netgrif.application.engine.auth.service;


import com.netgrif.application.engine.objects.preferences.Preferences;

public interface PreferencesService {

    Preferences get(String userId);

    Preferences save(Preferences preferences);
}