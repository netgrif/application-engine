package com.netgrif.application.engine.integrations.plugins.mock;

import com.netgrif.pluginlibrary.core.DeactivationRequest;
import com.netgrif.pluginlibrary.core.RegistrationRequest;
import com.netgrif.pluginlibrary.core.RegistrationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class MockPlugin {

    public static final String mockIdentifier = "mock_plugin";
    public static final String mockName = "mockPlugin";

    public static void registerOrActivatePlugin() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
                .usePlaintext()
                .build();

        RegistrationServiceGrpc.RegistrationServiceBlockingStub stub = RegistrationServiceGrpc.newBlockingStub(channel);
        stub.register(RegistrationRequest.newBuilder()
                .setIdentifier(mockIdentifier)
                .setName(mockName)
                .setUrl("mockurl")
                .setPort(9999)
                .build());
        channel.shutdown();
    }

    public static void deactivatePlugin() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
                .usePlaintext()
                .build();

        RegistrationServiceGrpc.RegistrationServiceBlockingStub stub = RegistrationServiceGrpc.newBlockingStub(channel);
        stub.deactivate(DeactivationRequest.newBuilder()
                .setIdentifier(mockIdentifier)
                .build());
        channel.shutdown();
    }
}
