package com.netgrif.application.engine.files.minio;

import lombok.Data;

import java.util.Map;

@Data
public class MinIoHostInfo {
    private String host;
    private Map<String, MinIoCredentials> credentials;

    public MinIoCredentials getCredentials(String credentialsKey) {
        return credentials.get(credentialsKey);
    }
}
