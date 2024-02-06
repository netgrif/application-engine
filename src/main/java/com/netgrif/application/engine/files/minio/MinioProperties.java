package com.netgrif.application.engine.files.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nae.minio")
public class MinioProperties {

    private String host;
    private String port;
    private String user;
    private String password;
    private String bucketName;
    private long partSize = 5242880L;

}

