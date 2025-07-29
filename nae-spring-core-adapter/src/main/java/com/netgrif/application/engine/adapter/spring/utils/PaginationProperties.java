package com.netgrif.application.engine.adapter.spring.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties class for pagination settings in the application.
 * This class retrieves and holds pagination-related properties defined under the {@code netgrif.pagination} prefix.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "netgrif.engine.pagination")
public class PaginationProperties {

    /**
     * The size of a single page for backend pagination.
     * This value determines the number of records or elements that should be fetched per page in backend-related operations.
     * The default value is {@code 100}.
     */
    private int backendPageSize = 100;

    /**
     * The size of a single page for frontend pagination.
     * This value determines the number of records or elements that should be displayed per page in the application's frontend.
     * The default value is {@code 20}.
     */
    private int frontendPageSize = 20;
}