package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.application.engine.integration.plugins.domain.EntryPoint;
import com.netgrif.application.engine.integration.plugins.domain.Method;
import com.netgrif.application.engine.integration.plugins.domain.Plugin;
import com.netgrif.pluginlibrary.core.MessageStatus;
import com.netgrif.pluginlibrary.core.RegistrationRequest;
import com.netgrif.pluginlibrary.core.RegistrationServiceGrpc;
import com.netgrif.pluginlibrary.core.ResponseMessage;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PluginRegistrationService extends RegistrationServiceGrpc.RegistrationServiceImplBase {
    private final IPluginService pluginService;

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
