package com.netgrif.application.engine.objects.elastic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class TaskField extends DataField {

    public String[] taskRefValue;

    public TaskField(String[] values) {
        super(values);
        this.taskRefValue = values;
    }

    @Override
    public Object getValue() {
        if (taskRefValue == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(taskRefValue));
    }
}
