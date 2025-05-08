package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.repository.PreferencesRepository;
import com.netgrif.application.engine.objects.preferences.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

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
        return repository.save(preferences);
    }
}