package com.netgrif.application.engine.auth.web.requestbodies;

import com.netgrif.application.engine.objects.auth.domain.Preferences;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreferencesRequest {

    private String userId;
    private String locale;
    private int drawerWidth;
    private Map<String, List<String>> taskFilters = new HashMap<>();
    private Map<String, List<String>> caseFilters = new HashMap<>();
    private Map<String, List<String>> headers = new HashMap<>();

    public Preferences toPreferences() {
        Preferences preferences = new com.netgrif.application.engine.adapter.spring.auth.domain.Preferences();
        preferences.setUserId(userId);
        preferences.setLocale(locale);
        preferences.setDrawerWidth(drawerWidth);
        preferences.setTaskFilters(taskFilters);
        preferences.setCaseFilters(caseFilters);
        preferences.setHeaders(headers);
        return preferences;
    }

}
