package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.dataset.FileField;
import org.springframework.stereotype.Component;

@Component
public class FileFieldBuilder extends FieldBuilder<FileField> {
    @Override
    public FileField build(Data data, Importer importer) {
        FileField fileField = new FileField();
        fileField.setRemote(data.getRemote() != null);
        setDefaultValue(fileField, data, defaultValue -> {
            if (defaultValue != null) {
                fileField.setDefaultValue(defaultValue);
            }
        });
        return fileField;
    }

    @Override
    public DataType getType() {
        return DataType.FILE;
    }
}
