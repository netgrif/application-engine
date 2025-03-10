package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.application.engine.integration.plugins.exceptions.InvalidRequestException;
import com.netgrif.application.engine.integration.plugins.exceptions.PluginIsAlreadyActiveException;
import com.netgrif.pluginlibrary.core.domain.EntryPoint;
import com.netgrif.pluginlibrary.core.domain.Method;
import com.netgrif.pluginlibrary.core.domain.Plugin;
import com.netgrif.pluginlibrary.core.utils.AbstractObjectParser;
import com.netgrif.pluginlibrary.service.services.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link com.netgrif.pluginlibrary.service.services.PluginRegistrationServiceGrpc.PluginRegistrationServiceImplBase}. This
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
public final class PluginRegistrationService extends PluginRegistrationServiceGrpc.PluginRegistrationServiceImplBase {
    private final IPluginService pluginService;

    /**
     * Registers or activate plugin provided by request.
     *
     * @param request request containing information about the plugin to be registered
     * */
    @Override
    public void register(RegistrationRequest request, StreamObserver<RegistrationResponse> responseObserver) {
        Plugin plugin = (Plugin) AbstractObjectParser.deserialize(request.getPlugin().toByteArray());
        try {
            validateRequest(plugin);
            String responseMsg = pluginService.registerOrActivate(plugin);
            RegistrationResponse response = RegistrationResponse.newBuilder().setMessage(responseMsg).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (PluginIsAlreadyActiveException | InvalidRequestException e) {
            log.error(e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.getMessage())));
        } catch (RuntimeException e) {
            String message = String.format("Something went wrong when registering or activating plugin with identifier [%s]",
                    plugin.getIdentifier());
            log.error(message, e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(message)));
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
            validateRequest(request);
            String responseMsg = pluginService.unregister(request.getIdentifier());
            UnregistrationResponse response = UnregistrationResponse.newBuilder().setMessage(responseMsg).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (InvalidRequestException e) {
            log.error(e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.getMessage())));
        } catch (RuntimeException e) {
            String message = String.format("Something went wrong when unregistering plugin with identifier [%s]",
                    request.getIdentifier());
            log.error(message, e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(message)));
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
            validateRequest(request);
            String responseMsg = pluginService.deactivate(request.getIdentifier());
            DeactivationResponse response = DeactivationResponse.newBuilder().setMessage(responseMsg).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (InvalidRequestException e) {
            log.error(e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(e.getMessage())));
        } catch (RuntimeException e) {
            String message = String.format("Something went wrong when deactivating plugin with identifier [%s]",
                    request.getIdentifier());
            log.error(message, e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL.withDescription(message)));
        }
    }

    private void validateRequest(Plugin request) throws InvalidRequestException {
        if (request.getIdentifier().equals(Strings.EMPTY)) {
            throw new InvalidRequestException("Plugin identifier is null or empty");
        }
        if (request.getName().equals(Strings.EMPTY)) {
            throw new InvalidRequestException("Plugin name is null or empty");
        }
        if (request.getUrl().equals(Strings.EMPTY)) {
            throw new InvalidRequestException("Plugin URL is null or empty");
        }
        for (EntryPoint ep : request.getEntryPoints().values()) {
            if (ep.getName().equals(Strings.EMPTY)) {
                throw new InvalidRequestException("Entry point name is null or empty");
            }
            for (Method m : ep.getMethods().values()) {
                if (m.getName().equals(Strings.EMPTY)) {
                    throw new InvalidRequestException("Method name is null or empty");
                }
            }
        }
    }

    private void validateRequest(UnregistrationRequest request) throws InvalidRequestException {
        if (request.getIdentifier().equals(Strings.EMPTY)) {
            throw new InvalidRequestException("Plugin identifier is null or empty");
        }
    }

    private void validateRequest(DeactivationRequest request) {
        if (request.getIdentifier().equals(Strings.EMPTY)) {
            throw new InvalidRequestException("Plugin identifier is null or empty");
        }
    }
}
