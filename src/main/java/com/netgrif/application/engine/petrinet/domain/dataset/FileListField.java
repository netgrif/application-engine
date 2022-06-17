package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.importer.model.DataType;
import lombok.Data;

import java.util.List;

@Data
public class FileListField extends Field<FileListFieldValue> {
    private Boolean remote;

    public FileListField() {
        super();
    }

    @Override
    public DataType getType() {
        return DataType.FILE_LIST;
    }

    public void setValue(String value) {
        this.setValue(FileListFieldValue.fromString(value));
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
        if (this.getValue() == null || this.getValue().getNamesPaths() == null) {
            this.setValue(new FileListFieldValue());
        }
        this.getValue().getNamesPaths().add(new FileFieldValue(fileName, path));
    }

    /**
     * Get complete file path to the file
     * Path is generated as follow:
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
            FileFieldValue first = this.getValue().getNamesPaths().find({namePath -> namePath.name == name});
            return first != null ? first.path : null;
        }
        return FileListFieldValue.getPath(caseId, getStringId(), name);
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
