package com.netgrif.application.engine.files.minio;

import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.RemoteStorageException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class MinioStorageService implements IStorageService {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioProperties properties;

    @Override
    public String getType() {
//        TODO enum
        return "MINIO";
    }

    @Override
    public InputStream get(String name) throws BadRequestException, ServiceErrorException {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(name)
                            .build()
            );
        } catch (ErrorResponseException e) {
            log.error(e.getMessage(), e);
            if (e.response().code() == 404) {
                return null;
            } else if (e.response().code() == 400) {
                throw new BadRequestException("Getting file from minio failed.", e);
            } else {
                throw new ServiceErrorException("Some http error from minio", e);
            }
        } catch (InvalidKeyException e) {
            log.error("Key " + name + " is corrupted.", e);
            throw new BadRequestException("Key " + name + " is corrupted.", e);
        } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException |
                 InvalidResponseException | XmlParserException | InternalException e) {
            log.error("Some internal error from minio", e);
            throw new ServiceErrorException("File cannot be get", e);
        }
    }

    @Override
    public boolean upload(String name, MultipartFile file) throws RemoteStorageException {
        try (InputStream stream = file.getInputStream()) {
            return this.upload(name, stream);
        } catch (RemoteStorageException | IOException e) {
            throw new RemoteStorageException("File cannot be save", e);
        }
    }

    @Override
    public boolean upload(String name, InputStream stream) throws RemoteStorageException {
        try {
            return minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(properties.getBucketName())
                    .object(name)
                    .stream(stream, -1, properties.getPartSize())
                    .build()).etag() != null;
        } catch (ErrorResponseException e) {
            log.error(e.getMessage(), e);
            throw new RemoteStorageException(e.getMessage(), e);
        } catch (InsufficientDataException | XmlParserException | InvalidKeyException | InternalException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException e) {
            log.error(e.getMessage(), e);
            throw new ServiceErrorException(e.getMessage());
        }
    }

    @Override
    public void delete(String name) throws RemoteStorageException {
        try {
            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket(properties.getBucketName())
                    .object(name)
                    .build());
        } catch (InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new ServiceErrorException(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ErrorResponseException e) {
            log.error("File cannot be deleted", e);
            throw new RemoteStorageException("File cannot be deleted", e);
        }
    }

}
