package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class TaskField extends DataField {

    public List<String> taskRefValue;

    public TaskField(List<String> values) {
        super(values);
        this.taskRefValue = values;
    }

    @Override
    public Object getValue() {
        if (taskRefValue == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(taskRefValue);
    }
}
