package com.netgrif.application.engine.integration.plugins.registration;

import com.netgrif.application.engine.integration.plugins.domain.EntryPoint;
import com.netgrif.application.engine.integration.plugins.domain.Method;
import com.netgrif.application.engine.integration.plugins.domain.Plugin;
import com.netgrif.application.engine.integration.plugins.properties.PluginRegistrationConfigProperties;
import com.netgrif.application.engine.integration.plugins.service.IPluginService;
import com.netgrif.pluginlibrary.register.MessageStatus;
import com.netgrif.pluginlibrary.register.RegistrationRequest;
import com.netgrif.pluginlibrary.register.RegistrationServiceGrpc;
import com.netgrif.pluginlibrary.register.ResponseMessage;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PluginRegistrationServer extends RegistrationServiceGrpc.RegistrationServiceImplBase {

    private final PluginRegistrationConfigProperties properties;
    private final IPluginService pluginService;
    private Server server;

    @PostConstruct
    public void startServer() throws IOException {
        server = ServerBuilder
                .forPort(properties.getPort())
                .addService(this).build();
        server.start();
        log.info("[gRPC Server] - Started on port " + properties.getPort());
    }

    @PreDestroy
    public void stopServer() {
        server.shutdown();
        log.info("[gRPC Server] - Started on port " + properties.getPort());
    }

    @Override
    public void register(RegistrationRequest request, StreamObserver<ResponseMessage> responseObserver) {
        ResponseMessage response;
        try {
            pluginService.register(convert(request));
            response = ResponseMessage.newBuilder()
                    .setStatus(MessageStatus.OK)
                    .setMessage("Plugin with identifier \"" + request.getIdentifier() + "\" was successfully registered.")
                    .build();
        } catch (IllegalArgumentException e) {
            response = ResponseMessage.newBuilder()
                    .setStatus(MessageStatus.NOT_OK)
                    .setMessage("Plugin with identifier \"" + request.getIdentifier() + "\" failed to register. " + e.getMessage())
                    .build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Plugin convert(RegistrationRequest request) {
        Plugin plugin = new Plugin();
        plugin.setIdentifier(request.getIdentifier());
        plugin.setUrl(request.getUrl());
        plugin.setEntryPoints(request.getEntryPointsList().stream().map(entryPoint -> {
            EntryPoint ep = new EntryPoint();
            ep.setIdentifier(entryPoint.getIdentifier());
            ep.setMethods(entryPoint.getMethodsList().stream().map(method -> {
                Method mth = new Method();
                mth.setName(method.getName());
                mth.setArgs(method.getArgsList());
                return mth;
            }).collect(Collectors.toMap(Method::getName, Function.identity())));
            return ep;
        }).collect(Collectors.toMap(EntryPoint::getIdentifier, Function.identity())));
        return plugin;
    }
}
