package com.netgrif.application.engine.workflow.domain.dataset;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Data
public class FileListFieldValue implements Serializable {

    private static final long serialVersionUID = 5299918326436821185L;

    private HashSet<FileFieldValue> namesPaths;

    public FileListFieldValue() {
        this(new HashSet<>());
    }

    public FileListFieldValue(HashSet<FileFieldValue> namesPaths) {
        this.namesPaths = namesPaths;
    }

    public static FileListFieldValue fromString(String value) {
        if (value == null) {
            value = "";
        }
        return buildValueFromParts(Arrays.asList(value.split(",")));
    }

    public static FileListFieldValue fromList(List<String> value) {
        return buildValueFromParts(value);
    }

    private static FileListFieldValue buildValueFromParts(List<String> parts) {
        FileListFieldValue newVal = new FileListFieldValue();
        for (String part : parts) {
            newVal.getNamesPaths().add(FileFieldValue.fromString(part));
        }
        return newVal;
    }

    @Override
    public String toString() {
        return namesPaths.toString();
    }
}
