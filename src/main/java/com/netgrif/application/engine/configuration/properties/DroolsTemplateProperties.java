package com.netgrif.application.engine.configuration.properties;

import lombok.Data;

@Data
public class DroolsTemplateProperties {

    private boolean generate;

    private String path;

    private String classpath;
}
