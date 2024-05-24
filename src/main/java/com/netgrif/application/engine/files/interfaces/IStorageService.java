package com.netgrif.application.engine.files.interfaces;

import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import com.netgrif.application.engine.files.throwable.StorageException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface IStorageService {
    String getType();

    InputStream get(String path) throws BadRequestException, ServiceErrorException, FileNotFoundException;

    boolean save(String path, MultipartFile file) throws StorageException;

    boolean save(String path, InputStream stream) throws StorageException;

    void delete(String path) throws StorageException;

    String getPreviewPath(String caseId, String fieldId, String name);

    String getPath(String caseId, String fieldId, String name);
}
