package com.netgrif.application.engine.auth.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.objects.preferences.Preferences;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreferencesResource {

    private Preferences preferences;
    private String error;
    private String message;

    public PreferencesResource(String error) {
        this.error = error;
    }

    public PreferencesResource(Preferences preferences) {
        this.preferences = preferences;
    }

    public static PreferencesResource withError(String errorMsg) {
        return new PreferencesResource(errorMsg);
    }

    public static PreferencesResource withPreferences(Preferences preferences) {
        return new PreferencesResource(preferences);
    }

    public static PreferencesResource withMessage(Preferences preferences, String message) {
        PreferencesResource preferencesResource = new PreferencesResource(preferences);
        preferencesResource.setMessage(message);
        return preferencesResource;
    }


}