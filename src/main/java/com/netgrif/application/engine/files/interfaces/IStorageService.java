package com.netgrif.application.engine.files.interfaces;

import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.petrinet.domain.dataset.Storage;
import com.netgrif.application.engine.petrinet.domain.dataset.StorageField;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface IStorageService {
    String getType();

    Storage createStorage(Data data);

    InputStream get(StorageField<?> field, String path) throws BadRequestException, ServiceErrorException, FileNotFoundException;

    boolean save(StorageField<?> field, String path, MultipartFile file) throws StorageException;

    boolean save(StorageField<?> field, String path, InputStream stream) throws StorageException;

    void delete(StorageField<?> field, String path) throws StorageException;

    String getPreviewPath(String caseId, String fieldId, String name);

    String getPath(String caseId, String fieldId, String name);
}
