package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.TaskField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TaskRefFieldBuilder extends FieldBuilder<TaskField> {
    @Override
    public TaskField build(Data data, Importer importer) {
        TaskField field = new TaskField();
        setDefaultValues(field, data, defaultValues -> {
            if (defaultValues != null && !defaultValues.isEmpty()) {
                List<String> defaults = new ArrayList<>();
                defaultValues.forEach(s -> {
                    if (importer.getDocument().getTransition().stream().noneMatch(t -> t.getId().equals(s)))
                        log.warn("There is no transition with id [" + s + "]");
                    defaults.add(s);
                });
                field.setDefaultValue(defaults);
            }
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.TASK_REF;
    }
}
