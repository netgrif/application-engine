package com.netgrif.application.engine.objects.dto.response.petrinet;


import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;

import java.io.Serializable;
import java.util.Locale;

public record DataFieldReferenceDto(String stringId,
                                    String title,
                                    String petriNetId,
                                    String transitionId,
                                    String type) implements Serializable {

    public static DataFieldReferenceDto fromField(Field field, Locale locale) {
        return new DataFieldReferenceDto(field.getStringId(), field.getTranslatedName(locale), null, null, field.getType().getName());
    }

    public static DataFieldReferenceDto fromField(Field field, Locale locale, String petriNetId, String transitionId) {
        return new DataFieldReferenceDto(field.getStringId(), field.getTranslatedName(locale), petriNetId, transitionId, field.getType().getName());
    }
}
