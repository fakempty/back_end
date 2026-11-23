package org.example.back_end1.lookup;

import com.sun.net.httpserver.HttpServer;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.common.grpc.lookup.PartAvailabilityResponse;
import org.example.common.grpc.lookup.PartIdRequest;
import org.example.common.grpc.lookup.PartLookupServiceGrpc;
import org.example.common.grpc.lookup.PartPriceResponse;
import org.example.common.grpc.lookup.PartStockResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class Lab9TransportAndErrorMappingTest {

    private HttpServer httpServer;
    private Server grpcServer;

    private static int freePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
        if (grpcServer != null) {
            grpcServer.shutdownNow();
        }
    }

    @Test
    void httpTransport_200_404_and_503() throws Exception {
        int port = freePort();
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/api/v2/lookup/parts/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String body;
            int code;
            if (path.endsWith("/1/price")) {
                code = 200;
                body = "{\"partId\":1,\"price\":25.00,\"currency\":\"USD\"}";
            } else if (path.endsWith("/1/stock")) {
                code = 200;
                body = "{\"partId\":1,\"quantity\":12,\"warehouse\":\"WH-MAIN\"}";
            } else if (path.endsWith("/1/availability")) {
                code = 200;
                body = "{\"partId\":1,\"available\":true,\"status\":\"IN_STOCK\"}";
            } else if (path.endsWith("/999/price")) {
                code = 404;
                body = "{\"error\":\"not found\"}";
            } else {
                code = 503;
                body = "{\"error\":\"unavailable\"}";
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        httpServer.start();

        Lab9LookupProperties props = new Lab9LookupProperties();
        props.setTransport("http");
        props.getRest().setBaseUrl("http://localhost:" + port);
        props.getRest().setConnectTimeout(Duration.ofMillis(500));
        props.getRest().setReadTimeout(Duration.ofMillis(1000));
        props.validateTransport();

        DeclarativePartRestClient client = new Lab9HttpClientConfig().declarativePartRestClient(props);
        HttpLookupAdapter adapter = new HttpLookupAdapter(client);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new Lab9PartLookupController(adapter, adapter, adapter))
                .setControllerAdvice(new LookupExceptionHandler())
                .build();

        mvc.perform(get("/api/v1/lab9/lookup/parts/1/price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partId").value(1))
                .andExpect(jsonPath("$.currency").value("USD"));

        mvc.perform(get("/api/v1/lab9/lookup/parts/1/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(12));

        mvc.perform(get("/api/v1/lab9/lookup/parts/1/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        mvc.perform(get("/api/v1/lab9/lookup/parts/999/price"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mvc.perform(get("/api/v1/lab9/lookup/parts/500/price"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void grpcTransport_200_404_and_503() throws Exception {
        int port = freePort();
        grpcServer = ServerBuilder.forPort(port)
                .addService(new PartLookupServiceGrpc.PartLookupServiceImplBase() {
                    @Override
                    public void getPartPrice(PartIdRequest request, StreamObserver<PartPriceResponse> responseObserver) {
                        if (request.getPartId() == 1L) {
                            responseObserver.onNext(PartPriceResponse.newBuilder()
                                    .setPartId(1L).setPrice("25.00").setCurrency("USD").build());
                            responseObserver.onCompleted();
                        } else {
                            responseObserver.onError(Status.NOT_FOUND.withDescription("Missing").asRuntimeException());
                        }
                    }

                    @Override
                    public void getPartStock(PartIdRequest request, StreamObserver<PartStockResponse> responseObserver) {
                        responseObserver.onNext(PartStockResponse.newBuilder()
                                .setPartId(1L).setQuantity(12).setWarehouse("WH-MAIN").build());
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void getPartAvailability(PartIdRequest request, StreamObserver<PartAvailabilityResponse> responseObserver) {
                        responseObserver.onNext(PartAvailabilityResponse.newBuilder()
                                .setPartId(1L).setAvailable(true).setStatus("IN_STOCK").build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();

        Lab9LookupProperties props = new Lab9LookupProperties();
        props.setTransport("grpc");
        props.getGrpc().setHost("localhost");
        props.getGrpc().setPort(port);
        props.getGrpc().setDeadline(Duration.ofMillis(1000));
        props.validateTransport();

        GrpcLookupAdapter adapter = new GrpcLookupAdapter(props);
        try {
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new Lab9PartLookupController(adapter, adapter, adapter))
                    .setControllerAdvice(new LookupExceptionHandler())
                    .build();

            mvc.perform(get("/api/v1/lab9/lookup/parts/1/price"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.partId").value(1));

            mvc.perform(get("/api/v1/lab9/lookup/parts/999/price"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));

            grpcServer.shutdownNow();
            mvc.perform(get("/api/v1/lab9/lookup/parts/1/price"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.status").value(503));
        } finally {
            adapter.closeChannel();
        }
    }

    @Test
    void invalidTransportFailsFast() {
        Lab9LookupProperties props = new Lab9LookupProperties();
        props.setTransport("unknown");
        assertThrows(IllegalStateException.class, props::validateTransport);
    }
}