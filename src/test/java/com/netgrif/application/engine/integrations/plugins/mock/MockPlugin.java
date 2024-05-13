package com.netgrif.application.engine.integrations.plugins.mock;

import com.google.protobuf.ByteString;
import com.netgrif.pluginlibrary.core.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.lang3.SerializationUtils;

public class MockPlugin {

    public static final String mockIdentifier = "mock_plugin";
    public static final String mockName = "mockPlugin";
    public static final String mockEntryPointName = "mockEntryPoint";
    public static final String mockMethodName = "mockMethodName";
    public static final Class<String> mockArgumentType = String.class;

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
                .addEntryPoints(EntryPoint.newBuilder()
                        .setName(mockEntryPointName)
                        .addMethods(Method.newBuilder()
                                .setName(mockMethodName)
                                .addArgs(ByteString.copyFrom(SerializationUtils.serialize(mockArgumentType)))
                                .build())
                        .build())
                .build());
        channel.shutdown();
    }

    public static void unregisterPlugin() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
                .usePlaintext()
                .build();

        RegistrationServiceGrpc.RegistrationServiceBlockingStub stub = RegistrationServiceGrpc.newBlockingStub(channel);
        stub.unregister(UnregistrationRequest.newBuilder()
                .setIdentifier(mockIdentifier)
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
