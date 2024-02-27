package com.netgrif.application.engine.settings.service;

import com.netgrif.application.engine.settings.domain.Preferences;
import com.netgrif.application.engine.settings.domain.PreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PreferencesService implements IPreferencesService {

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