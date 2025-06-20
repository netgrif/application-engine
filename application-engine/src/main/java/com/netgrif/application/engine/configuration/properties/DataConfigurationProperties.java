package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

/**
 * Configuration properties for the application engine's data functionality.
 * <p>
 * This class contains configurations specific to data management, particularly
 * related to REST API settings.
 */
@Data
@ConfigurationProperties(prefix = "netgrif.engine.data")
public class DataConfigurationProperties {

    private Rest rest = new Rest();

    /**
     * Configuration properties for the REST-specific settings under the
     * {@code netgrif.engine.data.rest} prefix.
     * <p>
     * This class extends {@link RepositoryRestProperties} to inherit
     * Spring Data REST functionalities while adding application-specific customizations.
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Rest extends RepositoryRestProperties {
    }
}
