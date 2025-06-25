package com.netgrif.application.engine.adapter.spring.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "netgrif.pagination")
public class PaginationProperties {

    private int backendPageSize = 100;

    private int frontendPageSize = 20;
}
