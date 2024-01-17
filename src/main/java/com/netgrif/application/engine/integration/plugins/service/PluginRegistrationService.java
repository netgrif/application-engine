package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.application.engine.integration.plugins.domain.EntryPoint;
import com.netgrif.application.engine.integration.plugins.domain.Method;
import com.netgrif.application.engine.integration.plugins.domain.Plugin;
import com.netgrif.pluginlibrary.core.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link com.netgrif.pluginlibrary.core.RegistrationServiceGrpc.RegistrationServiceImplBase}. This
 * serves as gRPC controller, that provides remotely executable functions.
 * */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "nae.plugin.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public final class PluginRegistrationService extends RegistrationServiceGrpc.RegistrationServiceImplBase {
    private final IPluginService pluginService;

    @Override
    public void register(RegistrationRequest request, StreamObserver<RegistrationResponse> responseObserver) {
        try {
            pluginService.register(convert(request));
            RegistrationResponse response = RegistrationResponse.newBuilder()
                    .setMessage("Plugin with identifier \"" + request.getIdentifier() + "\" was successfully registered.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void deactivate(DeactivationRequest request, StreamObserver<DeactivationResponse> responseObserver) {
        try {
            pluginService.deactivate(request.getIdentifier());
            DeactivationResponse response = DeactivationResponse.newBuilder()
                    .setMessage("Plugin with identifier \"" + request.getIdentifier() + "\" was successfully deactivated.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(e);
        }

    }

    private Plugin convert(RegistrationRequest request) {
        Plugin plugin = new Plugin();
        plugin.setIdentifier(request.getIdentifier());
        plugin.setName(request.getName());
        plugin.setUrl(request.getUrl());
        plugin.setPort(request.getPort());
        plugin.setActive(true);
        plugin.setEntryPoints(request.getEntryPointsList().stream().map(entryPoint -> {
            EntryPoint ep = new EntryPoint();
            ep.setName(entryPoint.getName());
            ep.setMethods(entryPoint.getMethodsList().stream().map(method -> {
                Method mth = new Method();
                mth.setName(method.getName());
                mth.setArgs(method.getArgsList().stream().map(arg -> (Class<?>) SerializationUtils.deserialize(arg.toByteArray())).collect(Collectors.toList()));
                return mth;
            }).collect(Collectors.toMap(Method::getName, Function.identity())));
            return ep;
        }).collect(Collectors.toMap(EntryPoint::getName, Function.identity())));
        return plugin;
    }
}
