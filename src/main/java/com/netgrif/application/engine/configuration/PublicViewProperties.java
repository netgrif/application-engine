package com.netgrif.application.engine.configuration;


import com.netgrif.application.engine.authentication.domain.PublicStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.public")
public class PublicViewProperties {

    private String url = "";
    private PublicStrategy strategy = PublicStrategy.SIMPLE;
}
