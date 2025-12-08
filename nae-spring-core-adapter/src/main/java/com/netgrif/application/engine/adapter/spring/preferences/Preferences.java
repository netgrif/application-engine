package com.netgrif.application.engine.adapter.spring.preferences;

import com.netgrif.application.engine.objects.dto.PreferencesDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;

@Document
public class Preferences extends com.netgrif.application.engine.objects.preferences.Preferences {

    public Preferences(String userId) {
        super(userId);
    }

    public static Preferences fromDto(PreferencesDto preferencesDto, String userId) {
        Preferences preferences = new Preferences(userId);
        preferences.setLocale(preferencesDto.locale());
        preferences.setDrawerWidth(preferencesDto.drawerWidth() <= 0 ? preferencesDto.drawerWidth() : 200);
        preferences.setTaskFilters(preferencesDto.taskFilters() != null ? preferencesDto.taskFilters() : new HashMap<>());
        preferences.setCaseFilters(preferencesDto.caseFilters() != null ? preferencesDto.caseFilters() : new HashMap<>());
        preferences.setHeaders(preferencesDto.headers() != null ? preferencesDto.headers() : new HashMap<>());
        return preferences;
    }
}
