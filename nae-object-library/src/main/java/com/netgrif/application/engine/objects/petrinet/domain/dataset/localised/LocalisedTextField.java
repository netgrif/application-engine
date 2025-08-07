package com.netgrif.application.engine.objects.petrinet.domain.dataset.localised;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.TextField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalisedTextField extends LocalisedField {

    private String subType;

    private Integer maxLength;

    private String formatting;

    public LocalisedTextField(TextField field, Locale locale) {
        super(field, locale);
        this.subType = field.getSubType();
        this.maxLength = field.getMaxLength();
        this.formatting = field.getFormatting();
    }
}
