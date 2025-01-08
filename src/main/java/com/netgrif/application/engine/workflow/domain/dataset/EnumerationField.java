package com.netgrif.application.engine.workflow.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.I18nString;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Locale;

@EqualsAndHashCode(callSuper = true)
@Data
public class EnumerationField extends ChoiceField<I18nString> {

    public EnumerationField() {
        super();
    }

    public EnumerationField(List<I18nString> choices) {
        super(choices);
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.ENUMERATION;
    }

    public String getTranslatedValue(Locale locale) {
        if (this.getValue() == null) {
            return null;
        }
        return getValue().getValue().getTranslation(locale);
    }

    @Override
    public EnumerationField clone() {
        EnumerationField clone = new EnumerationField();
        super.clone(clone);
        clone.choices = this.choices;
        clone.choicesExpression = this.choicesExpression;
        return clone;
    }
}