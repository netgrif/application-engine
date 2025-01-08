package com.netgrif.application.engine.workflow.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class StringCollectionField extends Field<List<String>> {

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.STRING_COLLECTION;
    }

    @Override
    public Field<List<String>> clone() {
        StringCollectionField clone = new StringCollectionField();
        super.clone(clone);
        return clone;
    }
}

