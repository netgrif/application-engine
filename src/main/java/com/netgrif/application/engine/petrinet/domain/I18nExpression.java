package com.netgrif.application.engine.petrinet.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class I18nExpression extends I18nString {
    private boolean dynamic;

    public I18nExpression(String defaultValue) {
        super(defaultValue);
        this.dynamic = false;
    }

    public I18nExpression clone() {
        I18nExpression clone = new I18nExpression();
        clone.setKey(this.getKey());
        clone.setDefaultValue(this.getDefaultValue());
        clone.setTranslations(new HashMap<>(this.getTranslations()));
        clone.setDynamic(this.dynamic);
        return clone;
    }
}