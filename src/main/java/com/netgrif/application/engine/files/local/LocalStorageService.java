package com.netgrif.application.engine.files.local;

import com.netgrif.application.engine.files.StorageType;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
public class LocalStorageService implements IStorageService {
    @Override
    public String getType() {
        return StorageType.LOCAL.getType();
    }

    @Override
    public InputStream get(String name) throws BadRequestException, ServiceErrorException {
        return null;
    }

    @Override
    public boolean upload(String name, MultipartFile file) throws StorageException {
        return false;
    }

    @Override
    public boolean upload(String name, InputStream stream) throws StorageException {
        return false;
    }

    @Override
    public void delete(String name) throws StorageException {

    }
}
