package com.netgrif.application.engine.configuration;

import lombok.Data;

@Data
public class LoadModule {

    private String name;
    private String version;
    private String description;
    private String author;

    private String groupId;
    private String artifactId;
    private String url;
    private String scmConnection;
    private String scmUrl;
    private String buildTime;

    private String license;
    private String organization;
    private String organizationUrl;
    private String issueSystem;
    private String issueUrl;
    private String buildJdk;
}
