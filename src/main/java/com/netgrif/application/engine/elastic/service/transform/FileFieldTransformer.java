package com.netgrif.application.engine.elastic.service.transform;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.FileField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import org.springframework.stereotype.Component;

@Component
public class FileFieldTransformer extends ElasticDataFieldTransformer<FileField, com.netgrif.application.engine.elastic.domain.FileField> {

    @Override
    public com.netgrif.application.engine.elastic.domain.FileField transform(FileField caseField, FileField petriNetField) {
        FileFieldValue value = caseField.getValue().getValue();
        if (value == null) {
            return null;
        }
        return new com.netgrif.application.engine.elastic.domain.FileField(value);
    }

    @Override
    public DataType getType() {
        return DataType.FILE;
    }
}
