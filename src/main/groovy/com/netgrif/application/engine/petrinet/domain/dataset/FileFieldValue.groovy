//package com.netgrif.application.engine.petrinet.domain.dataset
//
//import com.netgrif.application.engine.configuration.ApplicationContextProvider
//import com.netgrif.application.engine.files.interfaces.IStorageService
//
//class FileFieldValue implements Serializable {
//
//    private static final long serialVersionUID = 1299918326436821185L
//
//    private String name
//
//    private String path
//
//    private String previewPath
//
//    FileFieldValue() {
//    }
//
//    FileFieldValue(String name, String path) {
//        this.name = name
//        this.path = path
//    }
//
//    FileFieldValue(String name, String path, String previewPath) {
//        this.name = name
//        this.path = path
//        this.previewPath = previewPath
//    }
//
//    static FileFieldValue fromString(String value) {
//        if (!value.contains(":"))
//            return new FileFieldValue(value, null)
//
//        String[] parts = value.split(":", 2)
//        return new FileFieldValue(parts[0], parts[1])
//    }
//
//    String getName() {
//        return name
//    }
//
//    void setName(String name) {
//        this.name = name
//    }
//
//    String getPath() {
//        return path
//    }
//
//    void setPath(String path) {
//        this.path = path
//    }
//
//    String getPath(String caseId, String fieldId, String storageProvider = null) {
//        IStorageService storageService
//        if (storageProvider != null) {
//            storageService = ApplicationContextProvider.getBean(storageProvider) as IStorageService
//        } else {
//            storageService = ApplicationContextProvider.getBean("localStorageService") as IStorageService
//        }
//        return storageService.getPath(caseId, fieldId, name)
//    }
//
//    String getPreviewPath(String caseId, String fieldId, boolean isRemote, String storageProvider = null) {
//        IStorageService storageService
//        if (isRemote && storageProvider == null) {
//            return "${caseId}-${fieldId}-${name}.file_preview"
//        } else if (storageProvider != null) {
//            storageService = ApplicationContextProvider.getBean(storageProvider) as IStorageService
//        } else {
//            storageService = ApplicationContextProvider.getBean("localStorageService") as IStorageService
//        }
//        return storageService.getPreviewPath(caseId, fieldId, name)
//    }
//
//    String getPreviewPath() {
//        return previewPath
//    }
//
//    void setPreviewPath(String previewPath) {
//        this.previewPath = previewPath
//    }
//
//    @Override
//    String toString() {
//        return path
//    }
//}
