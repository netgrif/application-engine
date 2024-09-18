package com.netgrif.application.engine.files.minio;

import lombok.Data;

import java.util.Map;

@Data
public class MinIoHostInfo {
    private String host;
    private String user;
    private String password;
}
