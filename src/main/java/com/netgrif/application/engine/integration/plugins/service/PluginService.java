package com.netgrif.application.engine.integration.plugins.service;

import com.google.protobuf.ByteString;
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
        log.info("[gRPC Server] - Started on port " + properties.getPort());
    }

    @Override
    public void register(Plugin plugin) {
        Plugin existingPlugin = pluginRepository.findByIdentifier(plugin.getIdentifier());
        if (existingPlugin != null) {
            throw new IllegalArgumentException("Plugin with identifier \"" + plugin.getIdentifier() + "\" cannot be registered. Plugin with this identifier has already been registered.");
        }
        pluginRepository.save(plugin);
        log.info("Plugin with identifier \"" + plugin.getIdentifier() + "\" was registered.");
    }

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
        channel.shutdown();
        return SerializationUtils.deserialize(responseMessage.getResponse().toByteArray());
    }

    @Override
    public void unregister(String identifier) {
        Plugin existingPlugin = pluginRepository.findByIdentifier(identifier);
        if (existingPlugin == null) {
            throw new IllegalArgumentException("Plugin with identifier \"" + identifier + "\" cannot be unregistered. Plugin with this identifier does not exist.");
        }
        pluginRepository.delete(existingPlugin);
        log.info("Plugin with identifier \"" + identifier + "\" was unregistered.");
    }


}
