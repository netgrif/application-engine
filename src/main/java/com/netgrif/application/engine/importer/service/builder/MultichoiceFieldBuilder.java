package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceField;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;

@Component
public class MultichoiceFieldBuilder extends ChoiceFieldBuilder<MultichoiceField, LinkedHashSet<I18nString>> {

    @Override
    public MultichoiceField build(Data data, Importer importer) {
        MultichoiceField field = new MultichoiceField();
        initialize(field);
        if (data.getOptions() != null) {
            setFieldOptions(field, data, importer);
        }
        setDefaultValue(field, data, s -> {
            LinkedHashSet<I18nString> value = new LinkedHashSet<>();
            value.add(new I18nString(s));
            return value;
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.MULTICHOICE;
    }
}
