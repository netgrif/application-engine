package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.I18nField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import org.springframework.stereotype.Component;

@Component
public class I18nFieldBuilder extends FieldBuilder<I18nField> {

    @Override
    public I18nField build(Data data, Importer importer) {
        I18nField field = new I18nField();
        initialize(field);
        setDefaultValue(field, data, I18nString::new);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.I_18_N;
    }
}
