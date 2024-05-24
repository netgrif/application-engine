package com.netgrif.application.engine.petrinet.domain.dataset


import org.springframework.data.mongodb.core.mapping.Document

@Document
class FileField extends Field<FileFieldValue> {

    private String storageType

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

    String getStorageType() {
        return this.storageType
    }

    void setStorageType(String storageType) {
        this.storageType = storageType
    }

    @Override
    Field clone() {
        FileField clone = new FileField()
        super.clone(clone)
        clone.storageType = this.storageType
        return clone
    }
}