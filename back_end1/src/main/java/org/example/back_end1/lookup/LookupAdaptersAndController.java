package org.example.back_end1.lookup;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import org.example.common.grpc.lookup.PartAvailabilityResponse;
import org.example.common.grpc.lookup.PartIdRequest;
import org.example.common.grpc.lookup.PartLookupServiceGrpc;
import org.example.common.grpc.lookup.PartPriceResponse;
import org.example.common.grpc.lookup.PartStockResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "lab9.transport", havingValue = "http", matchIfMissing = true)
class HttpLookupAdapter implements PartPriceLookup, PartStockLookup, PartAvailabilityLookup {

    private final DeclarativePartRestClient client;

    HttpLookupAdapter(DeclarativePartRestClient client) {
        this.client = client;
    }

    @Override
    public PriceResult lookupPrice(long partId) {
        try {
            DeclarativePartRestClient.RawPrice r = client.fetchPrice(partId);
            return new PriceResult(r.partId(), r.price(), r.currency());
        } catch (PartNotFoundException | LookupServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new LookupServiceUnavailableException("HTTP lookup failed for part " + partId, ex);
        }
    }

    @Override
    public StockResult lookupStock(long partId) {
        try {
            DeclarativePartRestClient.RawStock r = client.fetchStock(partId);
            return new StockResult(r.partId(), r.quantity(), r.warehouse());
        } catch (PartNotFoundException | LookupServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new LookupServiceUnavailableException("HTTP lookup failed for part " + partId, ex);
        }
    }

    @Override
    public AvailabilityResult lookupAvailability(long partId) {
        try {
            DeclarativePartRestClient.RawAvailability r = client.fetchAvailability(partId);
            return new AvailabilityResult(r.partId(), r.available(), r.status());
        } catch (PartNotFoundException | LookupServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new LookupServiceUnavailableException("HTTP lookup failed for part " + partId, ex);
        }
    }
}

@Component
@ConditionalOnProperty(name = "lab9.transport", havingValue = "grpc")
class GrpcLookupAdapter implements PartPriceLookup, PartStockLookup, PartAvailabilityLookup {

    private final ManagedChannel channel;
    private final PartLookupServiceGrpc.PartLookupServiceBlockingStub blockingStub;
    private final long deadlineMs;

    GrpcLookupAdapter(Lab9LookupProperties props) {
        this.channel = ManagedChannelBuilder
                .forAddress(props.getGrpc().getHost(), props.getGrpc().getPort())
                .usePlaintext()
                .build();
        this.blockingStub = PartLookupServiceGrpc.newBlockingStub(channel);
        this.deadlineMs = props.getGrpc().getDeadline().toMillis();
    }

    private PartLookupServiceGrpc.PartLookupServiceBlockingStub stubWithDeadline() {
        return blockingStub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
    }

    private RuntimeException mapGrpcError(StatusRuntimeException ex, long partId) {
        if (ex.getStatus().getCode() == Status.Code.NOT_FOUND) {
            return new PartNotFoundException("Part not found via gRPC: " + partId);
        }
        return new LookupServiceUnavailableException("gRPC lookup failed (" + ex.getStatus().getCode() + ") for part " + partId, ex);
    }

    @Override
    public PriceResult lookupPrice(long partId) {
        try {
            PartIdRequest req = PartIdRequest.newBuilder().setPartId(partId).build();
            PartPriceResponse res = stubWithDeadline().getPartPrice(req);
            return new PriceResult(res.getPartId(), new BigDecimal(res.getPrice()), res.getCurrency());
        } catch (StatusRuntimeException ex) {
            throw mapGrpcError(ex, partId);
        }
    }

    @Override
    public StockResult lookupStock(long partId) {
        try {
            PartIdRequest req = PartIdRequest.newBuilder().setPartId(partId).build();
            PartStockResponse res = stubWithDeadline().getPartStock(req);
            return new StockResult(res.getPartId(), res.getQuantity(), res.getWarehouse());
        } catch (StatusRuntimeException ex) {
            throw mapGrpcError(ex, partId);
        }
    }

    @Override
    public AvailabilityResult lookupAvailability(long partId) {
        try {
            PartIdRequest req = PartIdRequest.newBuilder().setPartId(partId).build();
            PartAvailabilityResponse res = stubWithDeadline().getPartAvailability(req);
            return new AvailabilityResult(res.getPartId(), res.getAvailable(), res.getStatus());
        } catch (StatusRuntimeException ex) {
            throw mapGrpcError(ex, partId);
        }
    }

    @PreDestroy
    void closeChannel() {
        channel.shutdownNow();
    }
}

@RestController
@RequestMapping("/api/v1/lab9/lookup/parts")
class Lab9PartLookupController {

    private final PartPriceLookup priceLookup;
    private final PartStockLookup stockLookup;
    private final PartAvailabilityLookup availabilityLookup;

    Lab9PartLookupController(
            PartPriceLookup priceLookup,
            PartStockLookup stockLookup,
            PartAvailabilityLookup availabilityLookup) {
        this.priceLookup = priceLookup;
        this.stockLookup = stockLookup;
        this.availabilityLookup = availabilityLookup;
    }

    @GetMapping("/{id}/price")
    PartPriceLookup.PriceResult getPrice(@PathVariable("id") long id) {
        return priceLookup.lookupPrice(id);
    }

    @GetMapping("/{id}/stock")
    PartStockLookup.StockResult getStock(@PathVariable("id") long id) {
        return stockLookup.lookupStock(id);
    }

    @GetMapping("/{id}/availability")
    PartAvailabilityLookup.AvailabilityResult getAvailability(@PathVariable("id") long id) {
        return availabilityLookup.lookupAvailability(id);
    }
}