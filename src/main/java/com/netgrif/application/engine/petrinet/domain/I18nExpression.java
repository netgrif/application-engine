package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Locale;

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

    public Expression<String> getExpression(Locale locale) {
        String translation = this.getTranslation(locale);
        if (this.dynamic) {
            return Expression.ofDynamic(translation);
        }
        return Expression.ofStatic(translation);
    }
}