package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

@Data
public class I18nField extends Field<I18nString> {

    public I18nField() {
        // TODO: release/7.0.0 clearValue?
        super();
    }

    @Override
    public void clearValue() {
        setRawValue(new I18nString());
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.I_18_N;
    }

    @Override
    public I18nField clone() {
        I18nField clone = new I18nField();
        // TODO: release/7.0.0 deep copy?
        super.clone(clone);
        return clone;
    }
}
