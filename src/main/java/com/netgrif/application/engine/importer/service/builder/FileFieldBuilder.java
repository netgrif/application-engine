package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.dataset.FileField;
import org.springframework.stereotype.Component;

@Component
public class FileFieldBuilder extends FieldBuilder<FileField> {

    @Override
    public FileField build(Data data, Importer importer) {
        FileField field = new FileField();
        initialize(field);
        setDefaultValue(field, data);
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.FILE;
    }
}
