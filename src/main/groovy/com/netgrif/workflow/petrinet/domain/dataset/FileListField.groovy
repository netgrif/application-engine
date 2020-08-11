package com.netgrif.workflow.petrinet.domain.dataset

import org.apache.commons.lang3.tuple.ImmutablePair

class FileListField extends ValidableField<FileListFieldValue> {
    private Boolean remote

    FileListField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.FILELIST
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    @Override
    void setValue(FileListFieldValue value) {
        super.setValue(value)
    }

    @Override
    void setDefaultValue(FileListFieldValue defaultValue) {
        super.setDefaultValue(defaultValue)
    }

    boolean addValue(String fileName) {
        if (this.getValue() == null || this.getValue().getNames() == null || this.getValue().getPaths() == null) {
            this.setValue(new FileListFieldValue())
        }

        if (!this.getValue().getNames().contains(fileName)) {
            this.getValue().getNames().push(fileName)
            return true
        }
        return false
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
        if (this.remote)
            return this.getValue().getPaths().get(this.getValue().getNames().indexOf(name))
        return this.getValue().getPath(caseId, getStringId(), name)
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
        clone.validations = this.validations
        clone.defaultValue = this.defaultValue

        return clone
    }
}
