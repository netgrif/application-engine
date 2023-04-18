package com.netgrif.application.engine.configuration.properties;

import lombok.Data;

@Data
public class MailRedirectProperties {

    private boolean ssl;

    private String host;

    private String port;
}
