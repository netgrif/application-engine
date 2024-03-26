package com.netgrif.application.engine.files.local;

import com.netgrif.application.engine.files.StorageType;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.petrinet.domain.dataset.FileField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListField;
import com.netgrif.application.engine.workflow.domain.Case;
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

    @Autowired
    FileStorageConfiguration fileStorageConfiguration;

    @Override
    public StorageType getType() {
        return StorageType.LOCAL;
    }

    @Override
    public InputStream get(FileListField field, String path) throws BadRequestException, ServiceErrorException {

        return null;
    }

    @Override
    public InputStream get(FileField field, Case useCase, boolean getPreview) throws BadRequestException, ServiceErrorException {
        try {
            if (getPreview) {
                    return new FileInputStream(getPreviewPath(useCase.getStringId(), field.getImportId(), field.getValue().getName()));
            }
            return new FileInputStream(field.getValue().getPath());
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public boolean save(FileField field, String path, MultipartFile file) throws StorageException {
        File savedFile = new File(path);
        try {
            savedFile.getParentFile().mkdirs();
            if (!savedFile.createNewFile()) {
                savedFile.delete();
                savedFile.createNewFile();
            }

            FileOutputStream fout = new FileOutputStream(savedFile);
            fout.write(file.getBytes());
            fout.close();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new EventNotExecutableException("File " + file.getName() + " could not be saved to file field " + field.getStringId(), e);
        }

        return true;
    }

    @Override
    public boolean save(FileField field, String path, InputStream stream) throws StorageException {
        return false;
    }

    @Override
    public void delete(FileField fileField, Case useCase) throws StorageException {
        new File(getPath(useCase.getStringId(), fileField.getStringId(), fileField.getValue().getName())).delete();
        new File(getPreviewPath(useCase.getStringId(), fileField.getStringId(), fileField.getValue().getName())).delete();
    }

    @Override
    public void delete(FileListField fileField, Case useCase, FileFieldValue fileFieldValue) throws StorageException {

    }

    @Override
    public boolean save(FileListField field, String path, MultipartFile file) throws StorageException {
        return false;
    }

    @Override
    public boolean save(FileListField field, String path, InputStream stream) throws StorageException {
        return false;
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
