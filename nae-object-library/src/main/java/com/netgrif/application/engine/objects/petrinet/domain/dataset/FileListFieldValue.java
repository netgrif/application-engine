package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Getter
public class FileListFieldValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 5299918326436821185L;
    private HashSet<FileFieldValue> namesPaths;

    public FileListFieldValue() {
        this.namesPaths = new HashSet<>();
    }

    public FileListFieldValue(HashSet<FileFieldValue> namesPaths) {
        this.namesPaths = namesPaths;
    }


    public static FileListFieldValue fromString(String value) {
        if (value == null) value = "";
        return buildValueFromParts(Arrays.asList(value.split(",")));
    }

    public static FileListFieldValue fromList(List<String> value) {
        return buildValueFromParts(value);
    }

    private static FileListFieldValue buildValueFromParts(List<String> parts) {
        FileListFieldValue newVal = new FileListFieldValue();
        for (String part : parts) {
            if (!part.contains(":")) newVal.getNamesPaths().add(new FileFieldValue(part, null));
            else {
                String[] filePart = part.split(":", 2);
                newVal.getNamesPaths().add(new FileFieldValue(filePart[0], filePart[1]));
            }
        }
        return newVal;
    }

    public void setNamesPaths(HashSet<FileFieldValue> namesPaths) {
        this.namesPaths = namesPaths;
    }

    @Override
    public String toString() {
        return namesPaths.toString();
    }
}
