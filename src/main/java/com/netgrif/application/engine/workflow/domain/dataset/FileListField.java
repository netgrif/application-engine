package com.netgrif.application.engine.workflow.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

import java.util.Optional;

@Data
public class FileListField extends Field<FileListFieldValue> {

    public FileListField() {
        super();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.FILE_LIST;
    }

    public void setValue(String value) {
        this.setRawValue(FileListFieldValue.fromString(value));
    }

    public void addValue(String fileName, String path) {
        if (this.getRawValue() == null || this.getRawValue().getNamesPaths() == null) {
            this.setRawValue(new FileListFieldValue());
        }
        this.getRawValue().getNamesPaths().add(new FileFieldValue(fileName, path));
    }

    /**
     * Get complete file path to the file
     * Path is generated as follows:
     * - if file is remote, path is field value / remote URI
     * - if file is local
     * - saved file path consists of Case id, slash field import id, slash original file name
     *
     * @param caseId
     * @param name
     * @return path to the saved file
     */
    public String getFilePath(String caseId, String name) {
        if (this.isRemote()) {
            Optional<FileFieldValue> first = this.getValue().getValue().getNamesPaths().stream().filter(fileFieldValue -> fileFieldValue.getName().equals(name)).findFirst();
            return first.map(FileFieldValue::getPath).orElse(null);
        }
        return FileStorageConfiguration.getPath(caseId, getStringId(), name);
    }

    public boolean isRemote() {
        return "true".equals(getProperties().get("remote"));
    }

    @Override
    public FileListField clone() {
        FileListField clone = new FileListField();
        super.clone(clone);
        return clone;
    }
}
