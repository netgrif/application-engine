package com.netgrif.application.engine.files.local;

import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Slf4j
@Service
public class LocalStorageService implements IStorageService {
    FileStorageConfiguration fileStorageConfiguration;

    @Autowired
    public void setFileStorageConfiguration(FileStorageConfiguration fileStorageConfiguration) {
        this.fileStorageConfiguration = fileStorageConfiguration;
    }

    @Override
    public String getType() {
        return "LOCAL";
    }

    @Override
    public InputStream get(String path) throws BadRequestException, ServiceErrorException, FileNotFoundException {
        return new FileInputStream(path);
    }

    @Override
    public boolean save(String path, MultipartFile file) throws StorageException {
        try (InputStream stream = file.getInputStream()) {
            return this.save(path, stream);
        } catch (StorageException | IOException e) {
            throw new StorageException("File cannot be saved", e);
        }
    }

    @Override
    public boolean save(String path, InputStream stream) throws StorageException {
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
    public void delete(String path) throws StorageException {
        new File(path).delete();
    }

    @Override
    public String getPreviewPath(String caseId, String fieldId, String name) {
        return fileStorageConfiguration.getStoragePath() + "/file_preview/" + caseId + "-" + fieldId + "-" + name;
    }

    @Override
    public String getPath(String caseId, String fieldId, String name) {
        return fileStorageConfiguration.getStoragePath() + "/" + caseId + "-" + fieldId + "-" + name;
    }

}
