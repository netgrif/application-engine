package com.netgrif.application.engine.objects.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;

import java.util.ArrayList;
import java.util.List;

public class I18nStringUtils {
    public static List<String> collectTranslations(I18nString i18nString) {
        List<String> translations = new ArrayList<>();
        if (i18nString == null) {
            return translations;
        }
        translations.add(i18nString.getDefaultValue());
        translations.addAll(i18nString.getTranslations().values());
        return translations;
    }
}
