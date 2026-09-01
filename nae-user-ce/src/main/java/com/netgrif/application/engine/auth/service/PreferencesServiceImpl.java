package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.repository.PreferencesRepository;
import com.netgrif.application.engine.objects.preferences.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class PreferencesServiceImpl implements PreferencesService {

    private PreferencesRepository repository;

    @Autowired
    public void setRepository(PreferencesRepository repository) {
        this.repository = repository;
    }

    @Override
    public Preferences get(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Preferences save(Preferences preferences) {
        if (preferences.getSorts() == null) {
            Preferences storedPreferences = repository.findByUserId(preferences.getUserId());
            preferences.setSorts(storedPreferences == null || storedPreferences.getSorts() == null
                    ? new HashMap<>()
                    : storedPreferences.getSorts());
        }
        return repository.save(preferences);
    }
}
