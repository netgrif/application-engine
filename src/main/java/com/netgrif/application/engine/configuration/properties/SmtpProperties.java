package com.netgrif.application.engine.configuration.properties;

import lombok.Data;

@Data
public class SmtpProperties {

    private boolean debug;

    private boolean auth;

    private boolean starttls;
}
