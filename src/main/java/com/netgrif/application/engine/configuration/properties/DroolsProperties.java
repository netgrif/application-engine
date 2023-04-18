package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "drools")
@Data
@Component
public class DroolsProperties {

    private DroolsTemplateProperties template;

    private DroolsKnowBaseProperties knowBase;

    private DroolsCompileProperties compile;
}
