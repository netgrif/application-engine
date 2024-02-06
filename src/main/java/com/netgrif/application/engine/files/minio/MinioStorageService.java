package com.netgrif.application.engine.files.minio;

import com.netgrif.application.engine.files.interfaces.IStorageService;
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
    public InputStream get(String name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(name)
                        .build()
        );
    }

    @Override
    public ObjectWriteResponse upload(String name, MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        try (InputStream stream = file.getInputStream()) {
            return minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(properties.getBucketName())
                    .object(name)
                    .stream(stream, -1, properties.getPartSize())
                    .build());
        } catch (Exception e) {
            throw new IllegalArgumentException("File cannot be save", e);
        }
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
