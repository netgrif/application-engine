package com.netgrif.application.engine.utils;

import com.netgrif.application.engine.importer.model.Tag;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.DynamicValidation;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImporterUtils {

    public static Map<String, String> buildTagsMap(List<Tag> tagsList) {
        Map<String, String> tags = new HashMap<>();
        if (tagsList != null) {
            tagsList.forEach(tag -> {
                tags.put(tag.getKey(), tag.getValue());
            });
        }
        return tags;
    }

    public static Validation makeValidation(String rule, I18nString message, boolean dynamic) {
        return dynamic ? new DynamicValidation(rule, message) : new Validation(rule, message);
    }
}
