package com.netgrif.application.engine.files.minio;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConditionalOnProperty(
        value = "nae.minio.enabled",
        matchIfMissing = true,
        havingValue = "true"
)
@ConfigurationProperties(prefix = "nae.minio")
public class MinioProperties {

    private String host;
    private String user;
    private String password;
    private String bucketName;
    // Minimal part size is 5MB=5242880
    private long partSize = 5242880L;

}

