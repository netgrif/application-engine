package com.netgrif.workflow.pluginmanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "nae.plugin")
@Component
public class PluginManagerProperties {

    @Getter
    @Setter
    protected String folder = "plugin";

}
