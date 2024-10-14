package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.Init;
import com.netgrif.application.engine.importer.model.Option;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MapOptionsFieldBuilder<T extends MapOptionsField<I18nString, V>, V> extends FieldBuilder<T> {

    public void setFieldOptions(MapOptionsField<I18nString, V> field, Data data, Importer importer) {
        if (data.getOptions() == null) {
            return;
        }
        Init initExpression = data.getOptions().getInit();
        if (initExpression != null) {
            setDynamicOptions(field, initExpression);
        } else {
            setStaticOptions(field, data, importer);
        }
    }

    private void setStaticOptions(MapOptionsField<I18nString, V> field, Data data, Importer importer) {
        List<Option> option = data.getOptions().getOption();
        if (option == null || option.isEmpty()) {
            return;
        }
        LinkedHashMap<String, I18nString> options = option.stream()
                .collect(Collectors.toMap(Option::getKey, importer::toI18NString, (o1, o2) -> o1, LinkedHashMap::new));
        field.setOptions(options);
    }

    private void setDynamicOptions(MapOptionsField<I18nString, V> field, Init initExpression) {
        field.setOptionsExpression(Expression.ofDynamic(initExpression.getValue()));
    }
}
