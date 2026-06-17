package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import java.util.ArrayList;
import java.util.List;

public class TaskField extends Field<List<String>> {

    public TaskField() {
        super();
        this.setDefaultValue(new ArrayList<>());
    }

    @Override
    public FieldType getType() {
        return FieldType.TASK_REF;
    }

    @Override
    public Field<?> clone() {
        TaskField clone = new TaskField();
        super.clone(clone);
        return clone;
    }
}
