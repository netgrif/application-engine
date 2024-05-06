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

import java.util.List;
import java.util.Map;
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


    /**
     * Registers or activate plugin provided by request.
     *
     * @param request request containing information about the plugin to be registered
     * */
    @Override
    public void register(RegistrationRequest request, StreamObserver<RegistrationResponse> responseObserver) {
        try {
            String responseMsg = pluginService.registerOrActivate(convertRequestToModel(request));
            RegistrationResponse response = RegistrationResponse.newBuilder().setMessage(responseMsg).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(e);
        }
    }

    /**
     * Deactivates plugin provided by request.
     *
     * @param request request containing information about the plugin to be deactivated
     * */
    @Override
    public void deactivate(DeactivationRequest request, StreamObserver<DeactivationResponse> responseObserver) {
        try {
            String responseMsg = pluginService.deactivate(request.getIdentifier());
            DeactivationResponse response = DeactivationResponse.newBuilder().setMessage(responseMsg).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(e);
        }

    }

    private Plugin convertRequestToModel(RegistrationRequest request) {
        Plugin plugin = new Plugin();
        plugin.setIdentifier(request.getIdentifier());
        plugin.setName(request.getName());
        plugin.setUrl(request.getUrl());
        plugin.setPort(request.getPort());
        plugin.setActive(true);
        plugin.setEntryPoints(convertEntryPointsFromRequest(request.getEntryPointsList()));
        return plugin;
    }

    private Map<String, EntryPoint> convertEntryPointsFromRequest(List<com.netgrif.pluginlibrary.core.EntryPoint> entryPoints) {
        return entryPoints.stream().map(epReq -> {
            EntryPoint epModel = new EntryPoint();
            epModel.setName(epReq.getName());
            epModel.setMethods(epReq.getMethodsList().stream().map(methodReq -> {
                Method methodModel = new Method();
                methodModel.setName(methodReq.getName());
                methodModel.setArgs(methodReq.getArgsList().stream()
                        .map(arg -> (Class<?>) SerializationUtils.deserialize(arg.toByteArray()))
                        .collect(Collectors.toList())
                );
                return methodModel;
            }).collect(Collectors.toMap(Method::getName, Function.identity())));
            return epModel;
        }).collect(Collectors.toMap(EntryPoint::getName, Function.identity()));
    }
}
