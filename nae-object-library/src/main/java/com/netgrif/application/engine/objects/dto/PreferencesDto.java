package com.netgrif.application.engine.objects.dto;

import com.netgrif.application.engine.objects.preferences.Preferences;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * DTO for {@link Preferences}
 */
public record PreferencesDto(String userId, String locale, int drawerWidth,
                             Map<String, List<String>> taskFilters,
                             Map<String, List<String>> caseFilters,
                             Map<String, List<String>> headers) implements Serializable{

    public static PreferencesDto fromPreferences(Preferences preferences) {
        return new PreferencesDto(preferences.getUserId(), preferences.getLocale(),
                preferences.getDrawerWidth(), preferences.getTaskFilters(), preferences.getCaseFilters(),
                preferences.getHeaders());
    }
}