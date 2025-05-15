package com.netgrif.application.engine.petrinet.config;

import lombok.Data;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "nae.petrinet.resources")
@Data
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefaultProcessResource {

    private Resource[] defaultProcesses;

}
