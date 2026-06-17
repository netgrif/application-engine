package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;

public class I18nField extends Field<I18nString> {

    public I18nField() {
        super();
    }

    @Override
    public void clearValue() {
        setValue(new I18nString());
    }

    @Override
    public FieldType getType() {
        return FieldType.I18N;
    }

    @Override
    public Field<?> clone() {
        I18nField clone = new I18nField();
        super.clone(clone);
        return clone;
    }
}
