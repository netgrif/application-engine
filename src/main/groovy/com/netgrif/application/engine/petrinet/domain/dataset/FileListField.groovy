package com.netgrif.application.engine.petrinet.domain.dataset

class FileListField extends StorageField<FileListFieldValue> {

    FileListField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.FILELIST
    }


    @Override
    void setValue(FileListFieldValue value) {
        if (value instanceof String)
            this.setValue((String) value)
        else
            super.setValue(value)
    }

    void setValue(String value) {
        this.setValue(FileListFieldValue.fromString(value))
    }

    @Override
    void setDefaultValue(FileListFieldValue defaultValue) {
        if (value instanceof String)
            this.setDefaultValue((String) value)
        else
            super.setDefaultValue(defaultValue)
    }

    void setDefaultValue(String defaultValue) {
        this.setDefaultValue(FileListFieldValue.fromString(defaultValue))
    }

    void setDefaultValue(List<String> defaultValues) {
        this.setDefaultValue(FileListFieldValue.fromList(defaultValues))
    }

    void addValue(String fileName, String path) {
        if (this.getValue() == null || this.getValue().getNamesPaths() == null) {
            this.setValue(new FileListFieldValue())
        }
        this.getValue().getNamesPaths().add(new FileFieldValue(fileName, path))
    }

    @Override
    Field clone() {
        FileListField clone = new FileListField()
        super.clone(clone)
        clone.storage = this.storage
        return clone
    }
}
