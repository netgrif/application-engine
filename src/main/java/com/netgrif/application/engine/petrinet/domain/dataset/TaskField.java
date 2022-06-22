package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskField extends Field<List<String>> {

    public TaskField() {
        super();
        this.defaultValue = new ArrayList<>();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.TASK_REF;
    }

    @Override
    public TaskField clone() {
        TaskField clone = new TaskField();
        super.clone(clone);
        return clone;
    }
}
