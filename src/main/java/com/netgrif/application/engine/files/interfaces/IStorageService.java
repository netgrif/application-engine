package com.netgrif.application.engine.files.interfaces;

import com.netgrif.application.engine.files.StorageType;
import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.petrinet.domain.dataset.FileField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListField;
import com.netgrif.application.engine.workflow.domain.Case;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface IStorageService {
    StorageType getType();

    InputStream get(FileListField field, String path) throws BadRequestException, ServiceErrorException;

    InputStream get(FileField field, Case useCase, boolean getPreview) throws BadRequestException, ServiceErrorException, FileNotFoundException;

    boolean save(FileField field, String path, MultipartFile file) throws StorageException;

    boolean save(FileField field, String path, InputStream stream) throws StorageException;

    void delete(FileField fileField, Case useCase) throws StorageException;

    void delete(FileListField fileField, Case useCase, FileFieldValue fileFieldValue) throws StorageException;

    boolean save(FileListField field, String path, MultipartFile file) throws StorageException;

    boolean save(FileListField field, String path, InputStream stream) throws StorageException;

    String getPreviewPath(String caseId, String fieldId, String name);

    String getPath(String caseId, String fieldId, String name);
}
