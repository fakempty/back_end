package org.example.back_end1.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.example.back_end1.lookup.LookupServiceUnavailableException;
import org.example.back_end1.lookup.PartAvailabilityLookup;
import org.example.back_end1.lookup.PartNotFoundException;
import org.example.back_end1.lookup.PartPriceLookup;
import org.example.back_end1.lookup.PartStockLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@Primary
class ResilientPartLookupDecorator implements PartPriceLookup, PartStockLookup, PartAvailabilityLookup {

    private static final Logger log = LoggerFactory.getLogger(ResilientPartLookupDecorator.class);
    private static final String CB_NAME = "partLookupCircuitBreaker";
    private static final String RETRY_NAME = "partLookupRetry";

    private final PartPriceLookup priceDelegate;
    private final PartStockLookup stockDelegate;
    private final PartAvailabilityLookup availabilityDelegate;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    ResilientPartLookupDecorator(
            @Qualifier("httpLookupAdapter") PartPriceLookup priceDelegate,
            @Qualifier("httpLookupAdapter") PartStockLookup stockDelegate,
            @Qualifier("httpLookupAdapter") PartAvailabilityLookup availabilityDelegate,
            RetryRegistry retryRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.priceDelegate = priceDelegate;
        this.stockDelegate = stockDelegate;
        this.availabilityDelegate = availabilityDelegate;
        this.retryRegistry = retryRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    void registerEventListeners() {
        Retry retry = retryRegistry.retry(RETRY_NAME);
        retry.getEventPublisher().onRetry(event ->
                log.warn("[Lab10 Retry] Attempt #{} for '{}' after transient failure: {}",
                        event.getNumberOfRetryAttempts(),
                        event.getName(),
                        event.getLastThrowable() != null ? event.getLastThrowable().getMessage() : "unknown")
        );

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(CB_NAME);
        cb.getEventPublisher().onStateTransition(event ->
                log.warn("[Lab10 CircuitBreaker] '{}' state transition: {} -> {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState())
        );
    }

    private <T> T executeWithResilience(String operation, long partId, Supplier<T> call) {
        Retry retry = retryRegistry.retry(RETRY_NAME);
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(CB_NAME);

        Supplier<T> decorated = CircuitBreaker.decorateSupplier(cb, call);
        decorated = Retry.decorateSupplier(retry, decorated);

        try {
            return decorated.get();
        } catch (PartNotFoundException ex) {
            throw ex;
        } catch (CallNotPermittedException ex) {
            log.warn("[Lab10 Fallback] Circuit breaker '{}' is OPEN; call rejected for '{}' partId={}",
                    CB_NAME, operation, partId);
            throw new CircuitOpenException(
                    "Lookup service unavailable: circuit breaker '" + CB_NAME + "' is currently OPEN",
                    ex
            );
        } catch (LookupServiceUnavailableException ex) {
            log.error("[Lab10 Fallback] '{}' failed for partId={} after retries: {}",
                    operation, partId, ex.getMessage());
            throw new CircuitOpenException(
                    "Lookup service unavailable for '" + operation + "' (partId=" + partId + "): " + ex.getMessage(),
                    ex
            );
        }
    }

    @Override
    public PriceResult lookupPrice(long partId) {
        return executeWithResilience("lookupPrice", partId, () -> priceDelegate.lookupPrice(partId));
    }

    @Override
    public StockResult lookupStock(long partId) {
        return executeWithResilience("lookupStock", partId, () -> stockDelegate.lookupStock(partId));
    }

    @Override
    public AvailabilityResult lookupAvailability(long partId) {
        return executeWithResilience("lookupAvailability", partId, () -> availabilityDelegate.lookupAvailability(partId));
    }
}