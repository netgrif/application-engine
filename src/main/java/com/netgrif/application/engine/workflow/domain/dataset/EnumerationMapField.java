package com.netgrif.application.engine.workflow.domain.dataset;


import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.I18nString;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

@Data
public class EnumerationMapField extends MapOptionsField<I18nString, String> {

    public EnumerationMapField() {
        super();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.ENUMERATION_MAP;
    }

    public I18nString getSelectedOption() {
        if (this.getOptions() == null) {
            return null;
        }
        return this.getOptions().get(this.getRawValue());
    }

    @Override
    public EnumerationMapField clone() {
        EnumerationMapField clone = new EnumerationMapField();
        super.clone(clone);
        clone.options = options;
        clone.optionsExpression = optionsExpression;
        return clone;
    }
}
