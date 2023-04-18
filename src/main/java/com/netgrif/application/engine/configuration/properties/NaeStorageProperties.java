package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nae.storage")
public class NaeStorageProperties {

    private boolean clean;

    private String archived;

    private String path;

    private String filePreviewFolder;
}
