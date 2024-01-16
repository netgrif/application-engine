package com.netgrif.application.engine.integration.plugins.service;

import com.google.protobuf.ByteString;
import com.netgrif.application.engine.integration.plugin.injector.PluginInjector;
import com.netgrif.application.engine.integration.plugins.domain.Plugin;
import com.netgrif.application.engine.integration.plugins.properties.PluginRegistrationConfigProperties;
import com.netgrif.application.engine.integration.plugins.repository.PluginRepository;
import com.netgrif.pluginlibrary.core.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base service that manages gRPC server on application startup, registers, activates and deactivates plugins, sends
 * plugin execution requests to desired plugin.
 * */
@Slf4j
@Service
@RequiredArgsConstructor
public class PluginService implements IPluginService {
    private final PluginRepository pluginRepository;
    private final PluginRegistrationConfigProperties properties;
    private Server server;

    @PostConstruct
    public void startServer() throws IOException {
        server = ServerBuilder
                .forPort(properties.getPort())
                .addService(new PluginRegistrationService(this))
                .build();
        server.start();
        log.info("[gRPC Server] - Started on port " + properties.getPort());
    }

    @PreDestroy
    public void stopServer() {
        server.shutdown();
        log.info("[gRPC Server] - Sopped server on port " + properties.getPort());
    }

    /**
     * @param plugin - plugin to be registered, or if already registered, then activate
     * */
    @Override
    public void register(Plugin plugin) {
        Plugin existingPlugin = pluginRepository.findByIdentifier(plugin.getIdentifier());
        if (existingPlugin != null) {
            log.warn("Plugin with identifier \"" + plugin.getIdentifier() + "\" has already been registered. Plugin will be activated.");
            plugin.set_id(existingPlugin.get_id());
        }
        pluginRepository.save(plugin);
        PluginInjector.inject(plugin);
        if (existingPlugin != null) {
            log.info("Plugin with identifier \"" + plugin.getIdentifier() + "\" was activated.");
        } else {
            log.info("Plugin with identifier \"" + plugin.getIdentifier() + "\" was registered.");
        }
    }

    /**
     * @param pluginId ID of plugin that contains the method that should be executed
     * @param entryPoint name of entry point in plugin that contains the method that should be executed
     * @param method name of method that should be executed
     * @param args arguments to send to plugin method. All args should be the exact type of method input arguments type (not superclass, or subclass)
     * @return the returned object of the executed plugin method
     * */
    @Override
    public Object call(String pluginId, String entryPoint, String method, Serializable... args) {
        Plugin plugin = pluginRepository.findByIdentifier(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin with identifier \"" + pluginId + "\" cannot be found");
        }
        ManagedChannel channel = ManagedChannelBuilder.forAddress(plugin.getUrl(), (int) plugin.getPort())
                .usePlaintext()
                .build();
        List<ByteString> argBytes = Arrays.stream(args).map(arg -> ByteString.copyFrom(SerializationUtils.serialize(arg))).collect(Collectors.toList());
        ExecutionServiceGrpc.ExecutionServiceBlockingStub stub = ExecutionServiceGrpc.newBlockingStub(channel);
        ExecutionResponse responseMessage = stub.execute(ExecutionRequest.newBuilder()
                .setEntryPoint(entryPoint)
                .setMethod(method)
                .addAllArgs(argBytes)
                .build());
        channel.shutdownNow();
        return SerializationUtils.deserialize(responseMessage.getResponse().toByteArray());
    }

    /**
     * @param identifier Identifier of plugin, that should be deactivated.
     * */
    @Override
    public void deactivate(String identifier) {
        Plugin existingPlugin = pluginRepository.findByIdentifier(identifier);
        if (existingPlugin == null) {
            throw new IllegalArgumentException("Plugin with identifier \"" + identifier + "\" cannot be deactivated. Plugin with this identifier does not exist.");
        }
        existingPlugin.setActive(false);
        pluginRepository.save(existingPlugin);
        log.info("Plugin with identifier \"" + identifier + "\" was deactivated.");
    }


}
