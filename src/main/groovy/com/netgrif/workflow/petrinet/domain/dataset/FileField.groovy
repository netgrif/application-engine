package com.netgrif.workflow.petrinet.domain.dataset


import org.springframework.data.mongodb.core.mapping.Document

@Document
class FileField extends FieldWithDefault<FileFieldValue> {

    private Boolean remote

    FileField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.FILE
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    void setValue(String value) {
        this.setValue(FileFieldValue.fromString(value))
    }

    void setDefaultValue(String defaultValue) {
        this.setDefaultValue(FileFieldValue.fromString(defaultValue))
    }

    /**
     * Get complete file path to the file
     * Path is generated as follow:
     * - if file is remote, path is field value / remote URI
     * - if file is local
     *    - always starts with directory storage/
     *    - saved file name consists of Case id, field import id and original file name separated by dash
     * @param caseId
     * @return path to the saved file
     */
    String getFilePath(String caseId) {
        if (this.remote)
            return this.getValue().getPath()
        return this.getValue().getPath(caseId, getStringId())
    }

    boolean isRemote() {
        return this.remote
    }

    void setRemote(boolean remote) {
        this.remote = remote
    }
}