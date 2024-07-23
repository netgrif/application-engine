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
        FileListField field = new FileListField();
        initialize(field);
        // TODO: NAE-1969 fix
//        field.setRemote(data.getRemote() != null);
        setDefaultValues(field, data, defaultValues -> {
            if (defaultValues != null && !defaultValues.isEmpty()) {
                field.setDefaultValue(defaultValues);
            }
        });
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.FILE_LIST;
    }
}
