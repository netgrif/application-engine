package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.MapOptionsField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedMapOptionsField<DV> extends LocalisedField {

    private Map<String, String> options;

    public LocalisedMapOptionsField(MapOptionsField<I18nString, DV> field, Locale locale) {
        super(field, locale);
        this.options = field.getOptions()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTranslation(locale), (o1, o2) -> o1, LinkedHashMap::new));
    }
}
