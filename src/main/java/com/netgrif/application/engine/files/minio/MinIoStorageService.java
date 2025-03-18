package com.netgrif.application.engine.files.minio;

import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.BadRequestException;
import com.netgrif.application.engine.files.throwable.ServiceErrorException;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.files.throwable.StorageNotEnabledException;
import com.netgrif.core.importer.model.Data;
import com.netgrif.core.petrinet.domain.dataset.MinIoStorage;
import com.netgrif.core.petrinet.domain.dataset.Storage;
import com.netgrif.core.petrinet.domain.dataset.StorageField;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@ConditionalOnProperty(
        value = "nae.storage.minio.enabled",
        havingValue = "true"
)
public class MinIoStorageService implements IStorageService {

    public static final String MINIO_TYPE = "minio";

    private MinIoProperties properties;

    @Autowired
    public void setProperties(MinIoProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getType() {
        return MINIO_TYPE;
    }

    @Override
    public Storage createStorage(Data data) {
        Storage storage = new MinIoStorage();
        if (!properties.isEnabled()) {
            throw new StorageNotEnabledException("Storage of type [" + MINIO_TYPE + "] is not enabled.");
        }
        if (data.getStorage().getHost() != null) {
            storage.setHost(data.getStorage().getHost());
        }
        if (data.getStorage().getBucket() != null) {
            ((MinIoStorage) storage).setBucket(getBucketOrDefault(data.getStorage().getBucket()));
        }
        return storage;
    }

    @Override
    public InputStream get(StorageField<?> field, String path) throws BadRequestException, ServiceErrorException, FileNotFoundException {
        MinIoStorage storage = (MinIoStorage) field.getStorage();
        try (MinioClient minioClient = client(storage.getHost())) {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(storage.getBucket())
                            .object(path)
                            .build()
            );
        } catch (ErrorResponseException e) {
            log.error(e.getMessage(), e);
            if (e.response().code() == 404) {
                throw new FileNotFoundException("File " + path + " not found.");
            } else if (e.response().code() == 400) {
                throw new BadRequestException("Getting file from minio failed.", e);
            } else {
                throw new ServiceErrorException("Some http error from minio", e);
            }
        } catch (InvalidKeyException e) {
            log.error("Key " + path + " is corrupted.", e);
            throw new BadRequestException("Key " + path + " is corrupted.", e);
        } catch (Exception e) {
            log.error("Some internal error from minio", e);
            throw new ServiceErrorException("The file cannot be retrieved", e);
        }
    }

    @Override
    public boolean save(StorageField<?> field, String path, MultipartFile file) throws StorageException {
        try (InputStream stream = file.getInputStream()) {
            return this.save(field, path, stream);
        } catch (StorageException | IOException e) {
            throw new StorageException("File cannot be saved", e);
        }
    }

    @Override
    public boolean save(StorageField<?> field, String path, InputStream stream) throws StorageException {
        MinIoStorage storage = (MinIoStorage) field.getStorage();
        try (MinioClient minioClient = client(storage.getHost())) {
            return minioClient.putObject(PutObjectArgs
                    .builder()
                    .bucket(storage.getBucket()).object(path)
                    .stream(stream, -1, properties.getPartSize())
                    .build()).etag() != null;
        } catch (ErrorResponseException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceErrorException(e.getMessage());
        }
    }

    @Override
    public void delete(StorageField<?> field, String path) throws StorageException {
        MinIoStorage storage = (MinIoStorage) field.getStorage();
        try (MinioClient minioClient = client(storage.getHost())) {
            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket(storage.getBucket())
                    .object(path)
                    .build());
        } catch (InsufficientDataException | InternalException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new ServiceErrorException(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            log.error("File cannot be deleted", e);
            throw new StorageException("File cannot be deleted", e);
        }
    }


    @Override
    public String getPreviewPath(String caseId, String fieldId, String name) {
        return caseId + "-" + fieldId + "-" + name + ".file_preview";
    }

    @Override
    public String getPath(String caseId, String fieldId, String name) {
        return caseId + "/" + fieldId + "-" + name;
    }

    public static String getBucketOrDefault(String bucket) {
        return bucket != null ? bucket : MinIoProperties.DEFAULT_BUCKET;
    }

    protected MinioClient client(String host) {
        return MinioClient.builder()
                .endpoint(properties.getHosts(host).getHost())
                .credentials(properties.getHosts(host).getUser(), properties.getHosts(host).getPassword())
                .build();
    }
}
