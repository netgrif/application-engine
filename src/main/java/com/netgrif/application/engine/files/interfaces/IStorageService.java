package com.netgrif.application.engine.files.interfaces;

import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.RemoteStorageException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface IStorageService {
    String getType();

    InputStream get(String name) throws BadRequestException, ServiceErrorException;

    boolean upload(String name, MultipartFile file) throws RemoteStorageException;

    boolean upload(String name, InputStream stream) throws RemoteStorageException;

    void delete(String name) throws RemoteStorageException;
}
