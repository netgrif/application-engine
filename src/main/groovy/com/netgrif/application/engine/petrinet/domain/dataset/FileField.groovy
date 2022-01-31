package com.netgrif.application.engine.petrinet.domain.dataset


import org.springframework.data.mongodb.core.mapping.Document

@Document
class FileField extends Field<FileFieldValue> {

    private Boolean remote

    FileField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.FILE
    }


    @Override
    void setValue(FileFieldValue value) {
        if (value instanceof String)
            this.setValue((String) value)
        else
            super.setValue(value)
    }

    void setValue(String value) {
        this.setValue(FileFieldValue.fromString(value))
    }

    @Override
    void setDefaultValue(FileFieldValue defaultValue) {
        if (value instanceof String)
            this.setDefaultValue((String) value)
        else
            super.setDefaultValue(defaultValue)
    }

    void setDefaultValue(String defaultValue) {
        this.setDefaultValue(FileFieldValue.fromString(defaultValue))
    }

    /**
     * Get complete file path to the file
     * Path is generated as follow:
     * - if file is remote, path is field value / remote URI
     * - if file is local
     *    - saved file name consists of Case id, field import id and original file name separated by dash
     * @param caseId
     * @return path to the saved file
     */
    String getFilePath(String caseId) {
        if (this.remote)
            return this.getValue().getPath()
        return this.getValue().getPath(caseId, getStringId())
    }

    String getFilePreviewPath(String caseId) {
        return this.getValue().getPreviewPath(caseId, getStringId())
    }

    boolean isRemote() {
        return this.remote
    }

    void setRemote(boolean remote) {
        this.remote = remote
    }

    @Override
    Field clone() {
        FileField clone = new FileField()
        super.clone(clone)
        clone.remote = this.remote

        return clone
    }
}