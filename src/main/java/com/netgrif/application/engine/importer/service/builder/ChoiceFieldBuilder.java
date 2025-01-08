package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.Init;
import com.netgrif.application.engine.importer.model.Option;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.dataset.ChoiceField;
import com.netgrif.application.engine.workflow.domain.dataset.logic.Expression;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ChoiceFieldBuilder<T extends ChoiceField<U>, U> extends FieldBuilder<T> {

    public void setFieldOptions(ChoiceField<?> field, Data data, Importer importer) {
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

    private void setStaticOptions(ChoiceField<?> field, Data data, Importer importer) {
        List<Option> option = data.getOptions().getOption();
        if (option == null || option.isEmpty()) {
            return;
        }
        LinkedHashSet<I18nString> options = option.stream()
                .map(importer::toI18NString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        field.setChoices(options);
    }

    private void setDynamicOptions(ChoiceField<?> field, Init initExpression) {
        field.setExpression(Expression.ofDynamic(initExpression.getValue()));
    }
}
