package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class CaseField extends FieldWithAllowedNets<List<String>> {

    public CaseField(List<String> allowedNets) {
        super(allowedNets);
    }

    /**
     * todo javadoc
     * */
    public static CaseField withValue(List<String> value) {
        CaseField field = new CaseField();
        field.setRawValue(value);
        return field;
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.CASE_REF;
    }

    @Override
    public CaseField clone() {
        CaseField clone = new CaseField();
        super.clone(clone);
        return clone;
    }
}