package com.netgrif.application.engine.petrinet.domain.dataset;


import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

@Data
public class FileField extends Field<FileFieldValue> {

    private Boolean remote;

    public FileField() {
        super();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.FILE;
    }

    public void setValue(String value) {
        this.setRawValue(FileFieldValue.fromString(value));
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
            return this.getValue().getValue().getPath();
        }
        return FileStorageConfiguration.getPath(caseId, getStringId(), this.getValue().getValue().getName());
    }

    public String getFilePreviewPath(String caseId) {
        return FileStorageConfiguration.getPreviewPath(caseId, getStringId(), this.getValue().getValue().getName());
    }

    @Override
    public FileField clone() {
        FileField clone = new FileField();
        super.clone(clone);
        clone.remote = this.remote;
        return clone;
    }

    public boolean isRemote() {
        return this.remote != null && this.remote;
    }
}