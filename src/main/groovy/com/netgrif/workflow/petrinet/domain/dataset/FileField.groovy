package com.netgrif.workflow.petrinet.domain.dataset


import org.springframework.data.mongodb.core.mapping.Document

@Document
class FileField extends FieldWithDefault<String> {

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
            return this.getValue()
        return "storage/${caseId}-${getStringId()}-${getValue()}"
    }

    boolean isRemote() {
        return this.remote
    }

    void setRemote(boolean remote) {
        this.remote = remote
    }
}