package com.netgrif.application.engine.petrinet.domain.dataset;


import com.netgrif.application.engine.importer.model.DataType;
import lombok.Data;

@Data
public class FileField extends Field<FileFieldValue> {

    private Boolean remote;

    public FileField() {
        super();
    }

    @Override
    public DataType getType() {
        return DataType.FILE;
    }

    public void setValue(String value) {
        this.setValue(FileFieldValue.fromString(value));
    }

    public void setDefaultValue(String defaultValue) {
        this.setDefaultValue(FileFieldValue.fromString(defaultValue));
    }

    /**
     * Get complete file path to the file
     * Path is generated as follow:
     * - if file is remote, path is field value / remote URI
     * - if file is local
     * - saved file name consists of Case id, field import id and original file name separated by dash
     *
     * @param caseId
     * @return path to the saved file
     */
    public String getFilePath(String caseId) {
        if (this.remote) {
            return this.getValue().getPath();
        }
        return this.getValue().getPath(caseId, getStringId());
    }

    public String getFilePreviewPath(String caseId) {
        return this.getValue().getPreviewPath(caseId, getStringId());
    }

    @Override
    public FileField clone() {
        FileField clone = new FileField();
        super.clone(clone);
        clone.remote = this.remote;
        return clone;
    }
}