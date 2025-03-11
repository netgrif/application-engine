package com.netgrif.application.engine.integration.plugins.config;

import com.netgrif.application.engine.integration.plugins.service.PluginRegistrationService;
import com.netgrif.application.engine.integration.plugins.service.PluginService;
import com.netgrif.pluginlibrary.core.properties.PluginConfigurationProperties;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PluginConfiguration {
    private final PluginConfigurationProperties pluginConfigurationProperties;
    private final ApplicationContext applicationContext;
    private static final String LOG_PREFIX = "[gRPC Server] -";

    /**
     * Configures gRPC server using given properties {@link PluginConfigurationProperties}
     * */
    @Bean
    public Server server(PluginService pluginService) throws IOException {
        Server server = ServerBuilder
                .forPort(pluginConfigurationProperties.getPluginGrpcPort())
                .addService(new PluginRegistrationService(pluginService))
                .build();
        log.info("{} Configured gRPC server on port {}", LOG_PREFIX, pluginConfigurationProperties.getPluginGrpcPort());
        return server;
    }
}
