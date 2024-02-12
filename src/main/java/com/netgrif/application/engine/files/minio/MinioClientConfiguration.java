package com.netgrif.application.engine.files.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value = "nae.minio.enabled",
        havingValue = "true"
)
public class MinioClientConfiguration {


    @Autowired
    private MinioProperties properties;


    @Bean
    MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getHost())
                .credentials(properties.getUser(), properties.getPassword())
                .build();
    }
}
