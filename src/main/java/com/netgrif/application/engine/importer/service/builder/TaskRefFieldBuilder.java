package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.dataset.TaskField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TaskRefFieldBuilder extends FieldBuilder<TaskField> {

    @Override
    public TaskField build(Data data, Importer importer) {
        TaskField field = new TaskField();
        initialize(field);
        setDefaultValue(field, data, List::of);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.TASK_REF;
    }
}
