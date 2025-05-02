package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.repository.PreferencesRepository;
import com.netgrif.application.engine.objects.auth.domain.Preferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public class PreferencesServiceImpl implements PreferencesService {

    @Autowired
    private PreferencesRepository repository;

    @Override
    public Preferences get(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Preferences save(Preferences preferences) {
        return repository.save(preferences);
    }
}