package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.ButtonField;

public class ButtonFieldBuilder extends FieldBuilder<ButtonField> {
    @Override
    public ButtonField build(Data data, Importer importer) {
        ButtonField field = new ButtonField();
        setDefaultValue(field, data, defaultValue -> {
            if (defaultValue != null) {
                field.setDefaultValue(Integer.parseInt(defaultValue));
            }
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.BUTTON;
    }
}
