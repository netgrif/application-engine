package com.netgrif.workflow.settings.service;

import com.netgrif.workflow.settings.domain.Preferences;
import com.netgrif.workflow.settings.domain.PreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PreferencesService implements IPreferencesService {

    @Autowired
    private PreferencesRepository repository;

    @Override
    public Preferences get(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Preferences save(Preferences preferences) {
        return repository.save(preferences);
    }
}