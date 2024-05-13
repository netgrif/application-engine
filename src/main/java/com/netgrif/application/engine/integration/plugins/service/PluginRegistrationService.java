package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.pluginlibrary.core.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

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
            String responseMsg = pluginService.registerOrActivate(request);
            RegistrationResponse response = RegistrationResponse.newBuilder().setMessage(responseMsg).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            log.error("Something went wrong when registering or activating plugin with identifier [{}]", request.getIdentifier(), e);
            responseObserver.onError(e);
        }
    }

    /**
     * Unregisters plugin provided by request.
     *
     * @param request request containing identifier of the plugin
     * */
    @Override
    public void unregister(UnregistrationRequest request, StreamObserver<UnregistrationResponse> responseObserver) {
        try {
            String responseMsg = pluginService.unregister(request.getIdentifier());
            UnregistrationResponse response = UnregistrationResponse.newBuilder().setMessage(responseMsg).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            log.error("Something went wrong when unregistering plugin with identifier [{}]", request.getIdentifier(), e);
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
        } catch (RuntimeException e) {
            log.error("Something went wrong when deactivating plugin with identifier [{}]", request.getIdentifier(), e);
            responseObserver.onError(e);
        }
    }
}
