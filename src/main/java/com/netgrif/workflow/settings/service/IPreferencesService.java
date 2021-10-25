package com.netgrif.workflow.settings.service;

import com.netgrif.workflow.settings.domain.Preferences;
import org.springframework.stereotype.Service;

public interface IPreferencesService {

    Preferences get(String userId);

    Preferences save(Preferences preferences);
}