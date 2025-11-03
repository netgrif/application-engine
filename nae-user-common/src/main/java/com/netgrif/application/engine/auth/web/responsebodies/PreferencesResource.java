package com.netgrif.application.engine.auth.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.objects.dto.PreferencesDto;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreferencesResource {

    private PreferencesDto preferences;
    private String error;
    private String message;

    public PreferencesResource(String error) {
        this.error = error;
    }

    public PreferencesResource(PreferencesDto preferences) {
        this.preferences = preferences;
    }

    public static PreferencesResource withError(String errorMsg) {
        return new PreferencesResource(errorMsg);
    }

    public static PreferencesResource withPreferences(PreferencesDto preferences) {
        return new PreferencesResource(preferences);
    }

    public static PreferencesResource withMessage(PreferencesDto preferences, String message) {
        PreferencesResource preferencesResource = new PreferencesResource(preferences);
        preferencesResource.setMessage(message);
        return preferencesResource;
    }


}