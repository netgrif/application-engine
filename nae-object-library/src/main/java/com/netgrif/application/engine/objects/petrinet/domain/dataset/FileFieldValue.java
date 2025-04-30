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

    public String getPath(String caseId, String fieldId, String storageProvider) {
//        IStorageService storageService;
//        if (storageProvider != null) {
//            storageService = (IStorageService) ApplicationContextProvider.invokeMethod("getBean", new Object[]{storageProvider});
//        } else {
//            storageService = (IStorageService) ApplicationContextProvider.invokeMethod("getBean", new Object[]{"localStorageService"});
//        }
//
//        return ((String) (storageService.invokeMethod("getPath", new Object[]{caseId, fieldId, getName()})));
        return "";
    }

    public String getPath(String caseId, String fieldId) {
        return getPath(caseId, fieldId, null);
    }

    public String getPreviewPath(String caseId, String fieldId, boolean isRemote, String storageProvider) {
//        IStorageService storageService;
//        if (isRemote && storageProvider == null) {
//            return caseId + "-" + fieldId + "-" + getName() + ".file_preview";
//        } else if (storageProvider != null) {
//            storageService = (IStorageService) ApplicationContextProvider.invokeMethod("getBean", new Object[]{storageProvider});
//        } else {
//            storageService = (IStorageService) ApplicationContextProvider.invokeMethod("getBean", new Object[]{"localStorageService"});
//        }
//
//        return ((String) (storageService.invokeMethod("getPreviewPath", new Object[]{caseId, fieldId, getName()})));
        return "";
    }

    public String getPreviewPath(String caseId, String fieldId, boolean isRemote) {
        return getPreviewPath(caseId, fieldId, isRemote, null);
    }

    @Override
    public String toString() {
        return path;
    }
}
