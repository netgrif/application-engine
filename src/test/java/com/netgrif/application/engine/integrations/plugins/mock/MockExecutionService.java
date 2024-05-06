package com.netgrif.application.engine.integrations.plugins.mock;

import com.google.protobuf.ByteString;
import com.netgrif.pluginlibrary.core.ExecutionRequest;
import com.netgrif.pluginlibrary.core.ExecutionResponse;
import com.netgrif.pluginlibrary.core.ExecutionServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Service
public class MockExecutionService extends ExecutionServiceGrpc.ExecutionServiceImplBase {

    private static final int port = 8090;
    private Server server;

    public ExecutionRequest lastExecutionRequest;

    @PostConstruct
    public void startServer() throws IOException {
        server = ServerBuilder
                .forPort(port)
                .addService(this)
                .build();
        server.start();
    }

    @PreDestroy
    public void stopServer() {
        server.shutdown();
    }

    @Override
    public void execute(ExecutionRequest request, StreamObserver<ExecutionResponse> responseObserver) {
        lastExecutionRequest = request;
        try {
            ExecutionResponse executionResponse = ExecutionResponse.newBuilder()
                    .setResponse(ByteString.copyFrom(SerializationUtils.serialize("mockResponse")))
                    .build();
            responseObserver.onNext(executionResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
