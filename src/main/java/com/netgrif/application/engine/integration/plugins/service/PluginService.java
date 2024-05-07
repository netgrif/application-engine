package com.netgrif.application.engine.integration.plugins.service;

import com.google.protobuf.ByteString;
import com.netgrif.application.engine.integration.plugin.injector.PluginInjector;
import com.netgrif.application.engine.integration.plugins.domain.Plugin;
import com.netgrif.application.engine.integration.plugins.properties.PluginConfigProperties;
import com.netgrif.application.engine.integration.plugins.repository.PluginRepository;
import com.netgrif.pluginlibrary.core.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base service, that manages gRPC server on application startup, registers, activates and deactivates plugins, sends
 * plugin execution requests to desired plugin.
 * */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "nae.plugin.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PluginService implements IPluginService {
    private final PluginRepository pluginRepository;
    private final PluginConfigProperties properties;
    private Server server;

    private static final String LOG_PREFIX = "[gRPC Server] -";

    @PostConstruct
    public void startServer() throws IOException {
        server = ServerBuilder
                .forPort(properties.getPort())
                .addService(new PluginRegistrationService(this))
                .build();
        server.start();
        log.info(LOG_PREFIX + " Started on port " + properties.getPort());
    }

    @PreDestroy
    public void stopServer() {
        server.shutdown();
        log.info(LOG_PREFIX + " Stopped server on port " + properties.getPort());
    }

    /**
     * Registers provided plugin into repository. If the plugin already exists, it's activated.
     * @param plugin - plugin to be registered, or if already registered, then activated
     *
     * @return activation or registration string message is returned
     * */
    @Override
    public String registerOrActivate(Plugin plugin) {
        Plugin existingPlugin = pluginRepository.findByIdentifier(plugin.getIdentifier());
        return existingPlugin == null ? register(plugin) : activate(plugin, existingPlugin.getId());
    }

    /**
     * Unregisters provided plugin from memory and database
     * @param identifier - identifier of the plugin to be unregistered
     *
     * @return unregistration string message is returned
     * */
    @Override
    public String unregister(String identifier) {
        Plugin plugin = pluginRepository.findByIdentifier(identifier);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin with identifier \"" + identifier + "\" cannot be found");
        }

        PluginInjector.uninject(plugin);
        pluginRepository.delete(plugin);

        String responseMsg = "Plugin with identifier \"" + identifier + "\" was unregistered.";
        log.info(responseMsg);
        return responseMsg;
    }

    /**
     * Calls method with arguments of a specified entry point
     *
     * @param pluginId plugin identifier, that contains the method to be executed
     * @param entryPoint name of entry point in plugin, that contains the method to be executed
     * @param method name of method to be executed
     * @param args arguments to send to plugin method. All args should be the exact type of method input arguments type (not superclass, or subclass)
     *
     * @return the returned object of the executed plugin method
     * */
    @Override
    public Object call(String pluginId, String entryPoint, String method, Serializable... args) throws IllegalArgumentException {
        Plugin plugin = pluginRepository.findByIdentifier(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin with identifier \"" + pluginId + "\" cannot be found");
        }
        if (!plugin.isActive()) {
            throw new IllegalArgumentException("Plugin with name \"" + plugin.getName() + "\" is deactivated");
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
     * Deactivates the plugin of the provided identifier
     *
     * @param identifier Identifier of the plugin, that should be deactivated.
     * */
    @Override
    public String deactivate(String identifier) throws IllegalArgumentException {
        Plugin existingPlugin = pluginRepository.findByIdentifier(identifier);
        if (existingPlugin == null) {
            throw new IllegalArgumentException("Plugin with identifier \"" + identifier + "\" cannot be deactivated. Plugin with this identifier does not exist.");
        }
        existingPlugin.setActive(false);
        pluginRepository.save(existingPlugin);

        String responseMsg = "Plugin with identifier \"" + identifier + "\" was deactivated.";
        log.info(responseMsg);
        return responseMsg;
    }

    /**
     * Finds all plugins in the database
     *
     * @return list of plugins
     * */
    public List<Plugin> findAll() {
        return pluginRepository.findAll();
    }

    private String register(Plugin plugin) {
        return saveAndInject(plugin, "registered");
    }

    private String activate(Plugin plugin, ObjectId existingPluginId) {
        plugin.setActive(true);
        plugin.setId(existingPluginId);
        return saveAndInject(plugin, "activated"); // we must also re-inject the plugin in case of there is a change of entry points
    }

    private String saveAndInject(Plugin plugin, String state) {
        pluginRepository.save(plugin);
        PluginInjector.inject(plugin);

        String responseMsg = "Plugin with identifier \"" + plugin.getIdentifier() + "\" was " + state + ".";
        log.info(responseMsg);
        return responseMsg;
    }

}
