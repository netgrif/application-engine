package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class MultichoiceField extends ChoiceField<Set<I18nString>> {

    public MultichoiceField() {
        super();
        super.setValue(new HashSet<>());
        super.setDefaultValue(new HashSet<>());
    }

    public MultichoiceField(List<I18nString> values) {
        this();
        this.choices.addAll(values);
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.MULTICHOICE;
    }

    @Override
    public MultichoiceField clone() {
        MultichoiceField clone = new MultichoiceField();
        super.clone(clone);
        clone.choices = this.choices;
        clone.choicesExpression = this.choicesExpression;
        return clone;
    }
}