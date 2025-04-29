package com.netgrif.application.engine.petrinet.service.workspace;

import com.netgrif.application.engine.objects.petrinet.domain.workspace.DefaultWorkspaceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultWorkspaceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DefaultWorkspaceService defaultWorkspaceService() {
        return new DefaultWorkspaceServiceImpl();
    }
}
