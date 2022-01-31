package com.netgrif.application.engine.petrinet.domain.dataset

class FileListField extends Field<FileListFieldValue> {
    private Boolean remote

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

    /**
     * Get complete file path to the file
     * Path is generated as follow:
     * - if file is remote, path is field value / remote URI
     * - if file is local
     *    - saved file path consists of Case id, slash field import id, slash original file name
     * @param caseId
     * @param name
     * @return path to the saved file
     */
    String getFilePath(String caseId, String name) {
        if (this.remote) {
            FileFieldValue first = this.getValue().getNamesPaths().find({ namePath -> namePath.name == name })
            return first != null ? first.path : null
        }
        return FileListFieldValue.getPath(caseId, getStringId(), name)
    }

    boolean isRemote() {
        return this.remote
    }

    void setRemote(boolean remote) {
        this.remote = remote
    }

    @Override
    Field clone() {
        FileListField clone = new FileListField()
        super.clone(clone)
        clone.remote = this.remote

        return clone
    }
}
