package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
public class FileFieldValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1299918326436821185L;
    private String name;
    private String path;
    private String previewPath;

    public FileFieldValue(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public FileFieldValue(String name, String path, String previewPath) {
        this.name = name;
        this.path = path;
        this.previewPath = previewPath;
    }

    public static FileFieldValue fromString(String value) {
        if (!value.contains(":")) return new FileFieldValue(value, null);

        String[] parts = value.split(":", 2);
        return new FileFieldValue(parts[0], parts[1]);
    }

    //todo: remove in later release
    /**
     * Deprecated. Use {com.netgrif.application.engine.files.interfaces.IStorageService#getPath(String, String, String)} instead.
     * This will be removed in future releases.
     *
     * @param caseId          the ID of the case
     * @param fieldId         the ID of the field
     * @param storageProvider optional storage provider name, can be null
     * @return empty string (method is deprecated and not functional)
     */
    @Deprecated
    public String getPath(String caseId, String fieldId, String storageProvider) {
        return "";
    }

    //todo: remove in later release
    /**
     * Deprecated. Use {com.netgrif.application.engine.files.interfaces.IStorageService#getPath(String, String)} instead. This will be removed
     * in later release.
     *
     * @param caseId  the ID of the case
     * @param fieldId the ID of the field
     * @return empty string (method is deprecated and not functional)
     */
    @Deprecated
    public String getPath(String caseId, String fieldId) {
        return getPath(caseId, fieldId, null);
    }


    @Override
    public String toString() {
        return path;
    }
}
