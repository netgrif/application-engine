package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceField;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MultichoiceFieldBuilder extends FieldBuilder<MultichoiceField> {

    @Override
    public MultichoiceField build(Data data, Importer importer) {
        MultichoiceField field = new MultichoiceField();
        initialize(field);
        if (data.getOptions() != null) {
            setFieldOptions(field, data, importer);
        } else {
            setFieldChoices(field, data, importer);
        }
        setDefaultValues(field, data, init -> {
            if (init != null && !init.isEmpty()) {
                field.setDefaultValue(init.stream().map(I18nString::new).collect(Collectors.toSet()));
            }
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.MULTICHOICE;
    }
}
