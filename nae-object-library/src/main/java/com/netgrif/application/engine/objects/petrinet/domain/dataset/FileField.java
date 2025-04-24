package com.netgrif.application.engine.objects.petrinet.domain.dataset;

public class FileField extends StorageField<FileFieldValue> {

    public FileField() {
        super();
    }

    @Override
    public FieldType getType() {
        return FieldType.FILE;
    }

    public void setValue(String value) {
        this.setValue(FileFieldValue.fromString(value));
    }

    public void setDefaultValue(String defaultValue) {
        this.setDefaultValue(FileFieldValue.fromString(defaultValue));
    }

    @Override
    public Field<?> clone() {
        FileField clone = new FileField();
        super.clone(clone);
        clone.setStorage(this.getStorage());
        return clone;
    }

}
