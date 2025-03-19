//package com.netgrif.application.engine.integrations.plugins.mock;
//
//import com.google.protobuf.ByteString;
//import com.netgrif.pluginlibrary.core.domain.EntryPoint;
//import com.netgrif.pluginlibrary.core.domain.Method;
//import com.netgrif.pluginlibrary.core.domain.Plugin;
//import com.netgrif.pluginlibrary.core.utils.AbstractObjectParser;
//import com.netgrif.pluginlibrary.services.service.*;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//
//import java.util.List;
//import java.util.Map;
//
//public class MockPlugin {
//
//    public static final String mockIdentifier = "mock_plugin";
//    public static String mockName = "mockPlugin";
//    public static String mockEntryPointName = "mockEntryPoint";
//    public static String mockMethodName = "mockMethodName";
//    public static final Class<String> mockArgumentType = String.class;
//    public static final Class<Double> mockOutputType = Double.class;
//
//    @SuppressWarnings("ResultOfMethodCallIgnored")
//    public static void registerOrActivatePlugin() {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
//                .usePlaintext()
//                .build();
//
//        PluginRegistrationServiceGrpc.PluginRegistrationServiceBlockingStub stub = PluginRegistrationServiceGrpc.newBlockingStub(channel);
//        Plugin plugin = Plugin.builder()
//                .identifier(mockIdentifier)
//                .name(mockName)
//                .url("mockurl")
//                .port(1)
//                .entryPoints(Map.of(
//                        "epName", EntryPoint.builder()
//                                .name(mockEntryPointName)
//                                .methods(Map.of(mockMethodName, Method.builder()
//                                        .name(mockMethodName)
//                                        .argTypes(List.of(mockArgumentType))
//                                        .build()))
//                                .build()
//                ))
//                .build();
//        stub.register(RegistrationRequest.newBuilder()
//                .setPlugin(ByteString.copyFrom(AbstractObjectParser.serialize(plugin)))
//                .build());
//        channel.shutdown();
//    }
//
//    @SuppressWarnings("ResultOfMethodCallIgnored")
//    public static void registerOrActivateWithCustomRequest(RegistrationRequest request) {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
//                .usePlaintext()
//                .build();
//
//        PluginRegistrationServiceGrpc.PluginRegistrationServiceBlockingStub stub = PluginRegistrationServiceGrpc.newBlockingStub(channel);
//        stub.register(request);
//        channel.shutdown();
//    }
//
//    @SuppressWarnings("ResultOfMethodCallIgnored")
//    public static void unregisterPlugin() {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
//                .usePlaintext()
//                .build();
//
//        PluginRegistrationServiceGrpc.PluginRegistrationServiceBlockingStub stub = PluginRegistrationServiceGrpc.newBlockingStub(channel);
//        stub.unregister(UnregistrationRequest.newBuilder()
//                .setIdentifier(mockIdentifier)
//                .build());
//        channel.shutdown();
//    }
//
//    @SuppressWarnings("ResultOfMethodCallIgnored")
//    public static void unregisterPluginWithCustomRequest(UnregistrationRequest request) {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
//                .usePlaintext()
//                .build();
//
//        PluginRegistrationServiceGrpc.PluginRegistrationServiceBlockingStub stub = PluginRegistrationServiceGrpc.newBlockingStub(channel);
//        stub.unregister(request);
//        channel.shutdown();
//    }
//
//    @SuppressWarnings("ResultOfMethodCallIgnored")
//    public static void deactivatePlugin() {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
//                .usePlaintext()
//                .build();
//
//        PluginRegistrationServiceGrpc.PluginRegistrationServiceBlockingStub stub = PluginRegistrationServiceGrpc.newBlockingStub(channel);
//        stub.deactivate(DeactivationRequest.newBuilder()
//                .setIdentifier(mockIdentifier)
//                .build());
//        channel.shutdown();
//    }
//
//    @SuppressWarnings("ResultOfMethodCallIgnored")
//    public static void deactivatePluginWithCustomRequest(DeactivationRequest request) {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
//                .usePlaintext()
//                .build();
//
//        PluginRegistrationServiceGrpc.PluginRegistrationServiceBlockingStub stub = PluginRegistrationServiceGrpc.newBlockingStub(channel);
//        stub.deactivate(request);
//        channel.shutdown();
//    }
//}
