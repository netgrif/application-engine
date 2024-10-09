package com.netgrif.application.engine.files.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "nae.storage.minio")
public class MinIoProperties {

    public static final String HOST = "host";
    public static final String DEFAULT_BUCKET = "default";

    private boolean enabled = false;
    private Map<String, MinIoHostInfo> hosts;
    /**
     * Minimal part size is 5MB=5242880
     * */
    private long partSize = 5242880L;

    public MinIoHostInfo getHosts(String host) {
        return hosts.get(host);
    }
}

