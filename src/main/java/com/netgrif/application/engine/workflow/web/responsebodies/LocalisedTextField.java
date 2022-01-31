package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import lombok.Data;

import java.util.Locale;

@Data
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