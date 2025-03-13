package com.netgrif.application.engine.integration.plugins.config;

import com.netgrif.application.engine.integration.plugin.injector.PluginInjector;
import com.netgrif.application.engine.integration.plugins.service.PluginService;
import com.netgrif.application.engine.integration.plugins.service.PluginServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PluginConfiguration {
    private static final String LOG_PREFIX = "[gRPC Server] -";

    @Bean
    @ConditionalOnMissingBean
    public PluginService pluginService() {
        return new PluginServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginInjector pluginInjector() {
        return new PluginInjector();
    }

//    /**
//     * Configures gRPC server using given properties {@link PluginConfigurationProperties}
//     * */
//    @Bean
//    public Server server(PluginService pluginService) throws IOException {
//        Server server = ServerBuilder
//                .forPort(pluginConfigurationProperties.getPluginGrpcPort())
//                .addService(new PluginRegistrationService(pluginService))
//                .build();
//        log.info("{} Configured gRPC server on port {}", LOG_PREFIX, pluginConfigurationProperties.getPluginGrpcPort());
//        return server;
//    }
}
