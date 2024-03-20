package com.netgrif.application.engine.files.interfaces;

import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface IStorageService {
    String getType();

    InputStream get(String name) throws BadRequestException, ServiceErrorException;

    boolean upload(String name, MultipartFile file) throws StorageException;

    boolean upload(String name, InputStream stream) throws StorageException;

    void delete(String name) throws StorageException;
}
