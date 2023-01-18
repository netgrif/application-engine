package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class FileListField extends Field<FileListFieldValue> {
    private Boolean remote;

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

    @Override
    public void setDefaultValue(FileListFieldValue defaultValue) {
        super.setDefaultValue(defaultValue);
    }

    public void setDefaultValue(String defaultValue) {
        this.setDefaultValue(FileListFieldValue.fromString(defaultValue));
    }

    public void setDefaultValue(List<String> defaultValues) {
        this.setDefaultValue(FileListFieldValue.fromList(defaultValues));
    }

    public void addValue(String fileName, String path) {
        if (this.getValue() == null || this.getValue().getValue().getNamesPaths() == null) {
            this.setRawValue(new FileListFieldValue());
        }
        this.getValue().getValue().getNamesPaths().add(new FileFieldValue(fileName, path));
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
        if (this.remote) {
            Optional<FileFieldValue> first = this.getValue().getValue().getNamesPaths().stream().filter(fileFieldValue -> fileFieldValue.getName().equals(name)).findFirst();
            if (first.isEmpty()) {
                return null;
            }
            return first.get().getPath();
        }
        return FileStorageConfiguration.getPath(caseId, getStringId(), name);
    }

    public boolean isRemote() {
        return this.remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    @Override
    public FileListField clone() {
        FileListField clone = new FileListField();
        super.clone(clone);
        clone.remote = this.remote;
        return clone;
    }
}
