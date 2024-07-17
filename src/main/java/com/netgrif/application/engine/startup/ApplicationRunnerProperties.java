package com.netgrif.application.engine.startup;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.runner")
public class ApplicationRunnerProperties {


}
