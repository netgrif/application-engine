package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.FilterField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedFilterField extends LocalisedField {

    private List<String> allowedNets;
    private Object filterMetadata;

    public LocalisedFilterField(FilterField field, Locale locale) {
        super(field, locale);
        this.allowedNets = field.getAllowedNets();
        this.filterMetadata = field.getFilterMetadata();
    }
}
