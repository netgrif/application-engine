package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.elastic.domain.FileField;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.dataset.FileListField;
import com.netgrif.application.engine.workflow.domain.dataset.FileListFieldValue;
import org.springframework.stereotype.Component;

@Component
public class FileListFieldTransformer extends ElasticDataFieldTransformer<FileListField, FileField> {

    @Override
    public FileField transform(FileListField caseField, FileListField petriNetField) {
        FileListFieldValue value = caseField.getValue().getValue();
        if (value == null) {
            return null;
        }
        return new FileField(value.getNamesPaths());
    }

    @Override
    public DataType getType() {
        return DataType.FILE_LIST;
    }
}
