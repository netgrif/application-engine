package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import java.util.List;

public class FileListField extends StorageField<FileListFieldValue> {

    public FileListField() {
        super();
    }

    @Override
    public FieldType getType() {
        return FieldType.FILELIST;
    }


    public void setValue(String value) {
        this.setValue(FileListFieldValue.fromString(value));
    }


    public void setDefaultValue(String defaultValue) {
        this.setDefaultValue(FileListFieldValue.fromString(defaultValue));
    }

    public void setDefaultValue(List<String> defaultValues) {
        this.setDefaultValue(FileListFieldValue.fromList(defaultValues));
    }

    public void addValue(String fileName, String path) {
        if (this.getValue() == null || this.getValue().getNamesPaths() == null) {
            this.setValue(new FileListFieldValue());
        }
        this.getValue().getNamesPaths().add(new FileFieldValue(fileName, path));
    }

    @Override
    public Field<?> clone() {
        FileListField clone = new FileListField();
        super.clone(clone);
        clone.setStorage(this.getStorage());
        return clone;
    }
}
