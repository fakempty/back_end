package org.example.back_end1.lookup;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Locale;

@Component
@ConfigurationProperties(prefix = "lab9")
class Lab9LookupProperties {
    private String transport = "http";
    private RestProps rest = new RestProps();
    private GrpcProps grpc = new GrpcProps();

    static class RestProps {
        private String baseUrl = "http://localhost:8082";
        private Duration connectTimeout = Duration.ofMillis(1000);
        private Duration readTimeout = Duration.ofMillis(2000);

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public Duration getConnectTimeout() { return connectTimeout; }
        public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
        public Duration getReadTimeout() { return readTimeout; }
        public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
    }

    static class GrpcProps {
        private String host = "localhost";
        private int port = 9092;
        private Duration deadline = Duration.ofMillis(2000);

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public Duration getDeadline() { return deadline; }
        public void setDeadline(Duration deadline) { this.deadline = deadline; }
    }

    @PostConstruct
    void validateTransport() {
        String mode = transport == null ? "" : transport.trim().toLowerCase(Locale.ROOT);
        if (!"http".equals(mode) && !"grpc".equals(mode)) {
            throw new IllegalStateException("Unsupported lab9.transport value: '" + transport + "'. Expected 'http' or 'grpc'.");
        }
    }

    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }
    public RestProps getRest() { return rest; }
    public void setRest(RestProps rest) { this.rest = rest; }
    public GrpcProps getGrpc() { return grpc; }
    public void setGrpc(GrpcProps grpc) { this.grpc = grpc; }
}

interface DeclarativePartRestClient {
    record RawPrice(long partId, BigDecimal price, String currency) {}
    record RawStock(long partId, int quantity, String warehouse) {}
    record RawAvailability(long partId, boolean available, String status) {}

    @GetExchange("/api/v2/lookup/parts/{id}/price")
    RawPrice fetchPrice(@PathVariable("id") long id);

    @GetExchange("/api/v2/lookup/parts/{id}/stock")
    RawStock fetchStock(@PathVariable("id") long id);

    @GetExchange("/api/v2/lookup/parts/{id}/availability")
    RawAvailability fetchAvailability(@PathVariable("id") long id);
}

@Configuration
class Lab9HttpClientConfig {

    @Bean
    DeclarativePartRestClient declarativePartRestClient(Lab9LookupProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) props.getRest().getConnectTimeout().toMillis());
        factory.setReadTimeout((int) props.getRest().getReadTimeout().toMillis());

        RestClient restClient = RestClient.builder()
                .baseUrl(props.getRest().getBaseUrl())
                .requestFactory(factory)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    if (res.getStatusCode().value() == 404) {
                        throw new PartNotFoundException("Part not found via HTTP");
                    }
                    throw new LookupServiceUnavailableException("Client error from target service: " + res.getStatusCode(), null);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new LookupServiceUnavailableException("Target service unavailable (5xx): " + res.getStatusCode(), null);
                })
                .build();

        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return proxyFactory.createClient(DeclarativePartRestClient.class);
    }
}