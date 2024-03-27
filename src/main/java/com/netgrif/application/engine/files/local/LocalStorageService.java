package com.netgrif.application.engine.files.local;

import com.netgrif.application.engine.files.StorageType;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.workflow.domain.EventNotExecutableException;
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
    public StorageType getType() {
        return StorageType.LOCAL;
    }

    @Override
    public InputStream get(String path) throws BadRequestException, ServiceErrorException {
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public boolean save(String path, MultipartFile file) throws StorageException {
        try (InputStream stream = file.getInputStream()) {
            return this.save(path, stream);
        } catch (StorageException | IOException e) {
            throw new StorageException("File cannot be save", e);
        }
    }

    @Override
    public boolean save(String path, InputStream stream) throws StorageException {
        File savedFile = new File(path);
        try {
            savedFile.getParentFile().mkdirs();
            if (!savedFile.createNewFile()) {
                savedFile.delete();
                savedFile.createNewFile();
            }
            FileOutputStream fout = new FileOutputStream(savedFile);
            stream.transferTo(fout);
            fout.close();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new EventNotExecutableException("File " + path + " could not be saved", e);
        }

        return true;
    }

    @Override
    public void delete(String path) throws StorageException {
//        TODO chyba v deletovani
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
