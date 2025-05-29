package com.netgrif.application.engine.settings.service;

import com.netgrif.application.engine.settings.domain.Preferences;
import com.netgrif.application.engine.settings.domain.PreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PreferencesService implements IPreferencesService {

    private final PreferencesRepository repository;

    @Override
    public Optional<Preferences> get(String identityId) {
        return repository.findByIdentityId(identityId);
    }

    @Override
    public Preferences save(Preferences preferences) {
        return repository.save(preferences);
    }
}