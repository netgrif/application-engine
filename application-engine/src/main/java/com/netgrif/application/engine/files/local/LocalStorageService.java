package com.netgrif.application.engine.files.local;

import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.minio.StorageConfigurationProperties;
import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.objects.importer.model.Data;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Storage;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.StorageField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

@Slf4j
@Service
public class LocalStorageService implements IStorageService {

    public static final String LOCAL_TYPE = "local";

    private StorageConfigurationProperties fileStorageConfiguration;

    @Autowired
    public void setFileStorageConfiguration(StorageConfigurationProperties fileStorageConfiguration) {
        this.fileStorageConfiguration = fileStorageConfiguration;
    }

    @Override
    public String getType() {
        return LOCAL_TYPE;
    }

    @Override
    public Storage createStorage(Data data) {
        return new Storage(LOCAL_TYPE);
    }

    @Override
    public InputStream get(StorageField<?> field, String path) throws BadRequestException, ServiceErrorException, FileNotFoundException {
        return new FileInputStream(path);
    }

    @Override
    public boolean save(StorageField<?> field, String path, MultipartFile file) throws StorageException {
        try (InputStream stream = file.getInputStream()) {
            return this.save(field, path, stream);
        } catch (StorageException | IOException e) {
            throw new StorageException("File cannot be saved", e);
        }
    }

    @Override
    public boolean save(StorageField<?> field, String path, InputStream stream) throws StorageException {
        File savedFile = createNewFile(path);
        try (FileOutputStream fout = new FileOutputStream(savedFile)) {
            stream.transferTo(fout);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new StorageException("File " + path + " could not be saved", e);
        }
        return true;
    }

    private File createNewFile(String path) throws StorageException {
        File savedFile = new File(path);
        savedFile.getParentFile().mkdirs();
        try {
            if (!savedFile.createNewFile()) {
                savedFile.delete();
                savedFile.createNewFile();
            }
        } catch (IOException e) {
            throw new StorageException("Empty file " + path + " could not be created", e);
        }
        return savedFile;
    }

    @Override
    public void delete(StorageField<?> field, String path) throws StorageException {
        new File(path).delete();
    }

    @Override
    public String getPreviewPath(String caseId, String fieldId, String name) {
        return resolveStoragePath("file_preview", caseId, getStorageFileName(fieldId, name));
    }

    @Override
    public String getPath(String caseId, String fieldId, String name) {
        return resolveStoragePath(caseId, getStorageFileName(fieldId, name));
    }

    private String getStorageFileName(String fieldId, String name) {
        validatePathElement(fieldId);
        validatePathElement(name);
        return fieldId + "-" + name;
    }

    private String resolveStoragePath(String... pathElements) {
        Path storageRoot;
        try {
            storageRoot = Path.of(fileStorageConfiguration.getPath()).normalize();
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid local storage path", e);
        }

        Path resolvedPath = storageRoot;
        for (String pathElement : pathElements) {
            validatePathElement(pathElement);
            resolvedPath = resolvedPath.resolve(pathElement);
        }

        Path normalizedPath = resolvedPath.normalize();
        Path absoluteStorageRoot = storageRoot.toAbsolutePath().normalize();
        if (!normalizedPath.toAbsolutePath().normalize().startsWith(absoluteStorageRoot)) {
            throw new IllegalArgumentException("Resolved path is outside of the local storage directory");
        }
        return normalizedPath.toString();
    }

    private void validatePathElement(String pathElement) {
        if (pathElement == null || pathElement.isBlank()
                || pathElement.equals(".") || pathElement.equals("..")
                || pathElement.indexOf('/') >= 0 || pathElement.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("Invalid local storage path element");
        }

        try {
            if (Path.of(pathElement).isAbsolute()) {
                throw new IllegalArgumentException("Absolute paths are not allowed in local storage path elements");
            }
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid local storage path element", e);
        }
    }

}
