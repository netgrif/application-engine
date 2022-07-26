package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListField;
import org.springframework.stereotype.Component;

@Component
public class FileListFieldBuilder extends FieldBuilder<FileListField> {
    @Override
    public FileListField build(Data data, Importer importer) {
        FileListField fileListField = new FileListField();
        fileListField.setRemote(data.getRemote() != null);
        setDefaultValues(fileListField, data, defaultValues -> {
            if (defaultValues != null && !defaultValues.isEmpty()) {
                fileListField.setDefaultValue(defaultValues);
            }
        });
        return fileListField;
    }

    @Override
    public DataType getType() {
        return DataType.FILE_LIST;
    }
}
