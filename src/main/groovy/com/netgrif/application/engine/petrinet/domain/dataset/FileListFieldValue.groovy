package com.netgrif.application.engine.petrinet.domain.dataset

class FileListFieldValue implements Serializable {

    private static final long serialVersionUID = 5299918326436821185L;

    private HashSet<FileFieldValue> namesPaths

    FileListFieldValue() {
        this.namesPaths = new HashSet<>()
    }

    HashSet<FileFieldValue> getNamesPaths() {
        return this.namesPaths
    }

    static FileListFieldValue fromString(String value) {
        if (value == null) value = ""
        return buildValueFromParts(Arrays.asList(value.split(",")))
    }

    static FileListFieldValue fromList(List<String> value) {
        return buildValueFromParts(value)
    }

    private static FileListFieldValue buildValueFromParts(List<String> parts) {
        FileListFieldValue newVal = new FileListFieldValue()
        for (String part : parts) {
            if (!part.contains(":"))
                newVal.getNamesPaths().add(new FileFieldValue(part, null))
            else {
                String[] filePart = part.split(":", 2)
                newVal.getNamesPaths().add(new FileFieldValue(filePart[0], filePart[1]))
            }
        }
        return newVal
    }

    @Override
    String toString() {
        return namesPaths.toString()
    }
}
