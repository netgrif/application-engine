package com.netgrif.application.engine.files.minio;

import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.workflow.domain.EventNotExecutableException;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioStorageService implements IStorageService {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioProperties properties;

    @Override
    public String getType() {
        return "MINIO";
    }

    @Override
    public InputStream get(String name) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(name)
                            .build()
            );
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                return null;
            } else {
                throw new EventNotExecutableException("File cannot be get", e);
            }
        } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new EventNotExecutableException("File cannot be get", e);
        }
    }

    @Override
    public ObjectWriteResponse upload(String name, MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            return this.upload(name, stream);
        } catch (Exception e) {
            throw new EventNotExecutableException("File cannot be save", e);
        }
    }

    @Override
    public ObjectWriteResponse upload(String name, InputStream stream) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.putObject(PutObjectArgs
                .builder()
                .bucket(properties.getBucketName())
                .object(name)
                .stream(stream, -1, properties.getPartSize())
                .build());
    }

    @Override
    public void delete(String name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(RemoveObjectArgs
                .builder()
                .bucket(properties.getBucketName())
                .object(name)
                .build());
    }

}
