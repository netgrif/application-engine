package com.netgrif.application.engine.files.minio;

import com.netgrif.application.engine.files.StorageType;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.petrinet.domain.dataset.FileField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListField;
import com.netgrif.application.engine.workflow.domain.Case;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.netgrif.application.engine.files.StorageType.MINIO;

@Slf4j
@Service
@ConditionalOnProperty(
        value = "nae.storage.minio.enabled",
        havingValue = "true"
)
public class MinioStorageService implements IStorageService {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioProperties properties;

    @Override
    public StorageType getType() {
        return MINIO;
    }

    @Override
    public InputStream get(FileListField field, String path) throws BadRequestException, ServiceErrorException {
//        field.getValue()
//        TODO resolve filename
        return get(path);
    }

    @Override
    public InputStream get(FileField field, Case useCase, boolean getPreview) throws BadRequestException, ServiceErrorException {
        String fieldValue = getPreview ? getPreviewPath(useCase.getStringId(), field.getImportId(), field.getValue().getName()) : getPath(useCase.getStringId(), field.getImportId(), field.getValue().getName());
        return this.get(fieldValue);
    }

    private InputStream get(String name) {
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
            throw new BadRequestException("Key " + name + " is corrupted.", e);
        } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException |
                 InvalidResponseException | XmlParserException | InternalException e) {
            log.error("Some internal error from minio", e);
            throw new ServiceErrorException("File cannot be get", e);
        }
    }

    @Override
    public boolean save(FileField field, String path, MultipartFile file) throws StorageException {
        try (InputStream stream = file.getInputStream()) {
            return this.save(field, path, stream);
        } catch (StorageException | IOException e) {
            throw new StorageException("File cannot be save", e);
        }
    }

    @Override
    public boolean save(FileField field, String path, InputStream stream) throws StorageException {
        try {
            return minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(properties.getBucketName()).object(path)
                    .stream(stream, -1, properties.getPartSize())
                    .build()).etag() != null;
        } catch (ErrorResponseException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage(), e);
        } catch (InsufficientDataException | XmlParserException | InvalidKeyException | InternalException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException e) {
            log.error(e.getMessage(), e);
            throw new ServiceErrorException(e.getMessage());
        }
    }

    @Override
    public void delete(FileField field, Case useCase) throws StorageException {
        try {
            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket(properties.getBucketName()).object(getPath(useCase.getStringId(), field.getImportId(), field.getValue().getName())).build());
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(properties.getBucketName()).object(getPreviewPath(useCase.getStringId(), field.getImportId(), field.getValue().getName()))
                    .build());
        } catch (InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new ServiceErrorException(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new BadRequestException(e.getMessage());
        } catch (ErrorResponseException e) {
            log.error("File cannot be deleted", e);
            throw new StorageException("File cannot be deleted", e);
        }
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
        return caseId + "-" + fieldId + "-" + name + ".file_preview";
    }

    @Override
    public String getPath(String caseId, String fieldId, String name) {
        return caseId + "-" + fieldId + "-" + name;
    }
}
