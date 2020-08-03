package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.MapChoiceField;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class LocalisedMapOptionsField<DV> extends LocalisedField {

    private Map<String, String> options;

    public LocalisedMapOptionsField(MapChoiceField<I18nString, DV> field, Locale locale) {
        super(field, locale);
        this.options = field.getChoices()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTranslation(locale), (o1, o2) -> o1, LinkedHashMap::new));
    }
}
