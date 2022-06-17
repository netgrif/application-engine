package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Data;

@Data
public class I18nField extends Field<I18nString> {

    public I18nField() {
        super();
    }

    @Override
    public DataType getType() {
        return DataType.I_18_N;
    }

    @Override
    public I18nField clone() {
        I18nField clone = new I18nField();
        super.clone(clone);
        return clone;
    }
}
