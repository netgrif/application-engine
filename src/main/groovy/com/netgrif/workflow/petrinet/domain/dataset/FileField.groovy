package com.netgrif.workflow.petrinet.domain.dataset


import org.springframework.data.mongodb.core.mapping.Document

@Document
class FileField extends FieldWithDefault<String> {

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
     * - always starts with directory storage/
     * - saved file name consists of Case id, field import id and original file name separated by dash
     * @param caseId
     * @return path to the saved file
     */
    String getFilePath(String caseId) {
        return "storage/${caseId}-${getStringId()}-${getValue()}"
    }
}