package api.petrinet.domain.dataset;

import java.util.HashSet;

public final class FileListFieldValueDto {

    private HashSet<FileFieldValueDto> namesPaths;

    public FileListFieldValueDto() {
    }

    public FileListFieldValueDto(HashSet<FileFieldValueDto> namesPaths) {
        this.namesPaths = namesPaths;
    }

    public HashSet<FileFieldValueDto> getNamesPaths() {
        return namesPaths;
    }

    public void setNamesPaths(HashSet<FileFieldValueDto> namesPaths) {
        this.namesPaths = namesPaths;
    }
}

