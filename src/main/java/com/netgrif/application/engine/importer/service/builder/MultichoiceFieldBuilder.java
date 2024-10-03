package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
public class MultichoiceFieldBuilder extends ChoiceFieldBuilder<MultichoiceField, LinkedHashSet<I18nString>> {

    @Override
    public MultichoiceField build(Data data, Importer importer) {
        MultichoiceField field = new MultichoiceField();
        initialize(field);
        if (data.getOptions() != null) {
            setFieldOptions(field, data, importer);
        }
//        setDefaultValue(field, data, init -> {
//            if (init != null && !init.isEmpty()) {
//                field.setDefaultValue(Expression.ofStatic(init));
//            }
//        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.MULTICHOICE;
    }
}
