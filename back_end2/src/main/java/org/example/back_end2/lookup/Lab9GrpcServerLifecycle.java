package org.example.back_end2.lookup;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.common.grpc.lookup.PartAvailabilityResponse;
import org.example.common.grpc.lookup.PartIdRequest;
import org.example.common.grpc.lookup.PartLookupServiceGrpc;
import org.example.common.grpc.lookup.PartPriceResponse;
import org.example.common.grpc.lookup.PartStockResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class Lab9GrpcServerLifecycle implements SmartLifecycle {

    private final PartLookupCatalogService catalog;
    private final boolean enabled;
    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Server server;

    public Lab9GrpcServerLifecycle(
            PartLookupCatalogService catalog,
            @Value("${lab9.grpc.server.enabled:false}") boolean enabled,
            @Value("${lab9.grpc.server.port:9092}") int port) {
        this.catalog = catalog;
        this.enabled = enabled;
        this.port = port;
    }

    @Override
    public void start() {
        if (!enabled || running.get()) {
            return;
        }
        try {
            this.server = ServerBuilder.forPort(port)
                    .addService(new PartLookupGrpcImpl(catalog))
                    .build()
                    .start();
            running.set(true);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start gRPC server on port " + port, e);
        }
    }

    @Override
    public void stop() {
        if (server != null && running.compareAndSet(true, false)) {
            server.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    private static class PartLookupGrpcImpl extends PartLookupServiceGrpc.PartLookupServiceImplBase {
        private final PartLookupCatalogService catalog;

        PartLookupGrpcImpl(PartLookupCatalogService catalog) {
            this.catalog = catalog;
        }

        @Override
        public void getPartPrice(PartIdRequest request, StreamObserver<PartPriceResponse> responseObserver) {
            catalog.findById(request.getPartId()).ifPresentOrElse(
                    item -> {
                        PartPriceResponse resp = PartPriceResponse.newBuilder()
                                .setPartId(item.id())
                                .setPrice(item.price().toPlainString())
                                .setCurrency(item.currency())
                                .build();
                        responseObserver.onNext(resp);
                        responseObserver.onCompleted();
                    },
                    () -> responseObserver.onError(Status.NOT_FOUND
                            .withDescription("Part not found: " + request.getPartId())
                            .asRuntimeException())
            );
        }

        @Override
        public void getPartStock(PartIdRequest request, StreamObserver<PartStockResponse> responseObserver) {
            catalog.findById(request.getPartId()).ifPresentOrElse(
                    item -> {
                        PartStockResponse resp = PartStockResponse.newBuilder()
                                .setPartId(item.id())
                                .setQuantity(item.quantity())
                                .setWarehouse(item.warehouse())
                                .build();
                        responseObserver.onNext(resp);
                        responseObserver.onCompleted();
                    },
                    () -> responseObserver.onError(Status.NOT_FOUND
                            .withDescription("Part not found: " + request.getPartId())
                            .asRuntimeException())
            );
        }

        @Override
        public void getPartAvailability(PartIdRequest request, StreamObserver<PartAvailabilityResponse> responseObserver) {
            catalog.findById(request.getPartId()).ifPresentOrElse(
                    item -> {
                        PartAvailabilityResponse resp = PartAvailabilityResponse.newBuilder()
                                .setPartId(item.id())
                                .setAvailable(item.available())
                                .setStatus(item.status())
                                .build();
                        responseObserver.onNext(resp);
                        responseObserver.onCompleted();
                    },
                    () -> responseObserver.onError(Status.NOT_FOUND
                            .withDescription("Part not found: " + request.getPartId())
                            .asRuntimeException())
            );
        }
    }
}