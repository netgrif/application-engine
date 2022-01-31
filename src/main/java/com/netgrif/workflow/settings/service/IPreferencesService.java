package com.netgrif.workflow.settings.service;

import com.netgrif.workflow.settings.domain.Preferences;
import org.springframework.stereotype.Service;

@Service
public interface IPreferencesService {
    Preferences get(Long userId);

    Preferences save(Preferences preferences);
}