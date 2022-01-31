package com.netgrif.workflow.configuration;

import com.netgrif.workflow.orgstructure.domain.Group;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@ConditionalOnProperty(value = "spring.data.rest.enabled", matchIfMissing = true)
@Configuration
public class RepositoryConfig extends RepositoryRestConfigurerAdapter {

    private final Class[] classes = {
            Group.class
    };

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(classes);
    }
}