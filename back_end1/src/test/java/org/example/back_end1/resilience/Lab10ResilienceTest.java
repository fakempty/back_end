package org.example.back_end1.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.example.back_end1.lookup.LookupServiceUnavailableException;
import org.example.back_end1.lookup.PartNotFoundException;
import org.example.back_end1.lookup.PartPriceLookup;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class Lab10ResilienceTest {

    @RestController
    static class TestLookupController {
        private final PartPriceLookup priceLookup;
        TestLookupController(PartPriceLookup priceLookup) {
            this.priceLookup = priceLookup;
        }
        @GetMapping("/test/parts/{id}/price")
        PartPriceLookup.PriceResult price(@PathVariable("id") long id) {
            return priceLookup.lookupPrice(id);
        }
    }

    @Test
    void retryRecoversFromTransientError_andNotFoundDoesNotRetry() {
        AtomicInteger calls = new AtomicInteger(0);
        PartPriceLookup delegate = id -> {
            int n = calls.incrementAndGet();
            if (id == 404L) {
                throw new PartNotFoundException("Not found");
            }
            if (n < 3) {
                throw new LookupServiceUnavailableException("Temporary glitch", null);
            }
            return new PartPriceLookup.PriceResult(id, new BigDecimal("49.99"), "USD");
        };

        RetryRegistry retryReg = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(10))
                .retryExceptions(LookupServiceUnavailableException.class)
                .ignoreExceptions(PartNotFoundException.class)
                .build());

        CircuitBreakerRegistry cbReg = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(6)
                .failureRateThreshold(50)
                .ignoreExceptions(PartNotFoundException.class)
                .build());

        ResilientPartLookupDecorator decorator = new ResilientPartLookupDecorator(
                delegate,
                id -> { throw new UnsupportedOperationException(); },
                id -> { throw new UnsupportedOperationException(); },
                retryReg,
                cbReg
        );

        PartPriceLookup.PriceResult result = decorator.lookupPrice(1L);
        assertEquals(new BigDecimal("49.99"), result.price());
        assertEquals(3, calls.get());

        calls.set(0);
        assertThrows(PartNotFoundException.class, () -> decorator.lookupPrice(404L));
        assertEquals(1, calls.get());
    }

    @Test
    void openCircuitMapsTo503ViaExceptionHandler() throws Exception {
        PartPriceLookup failingDelegate = id -> {
            throw new LookupServiceUnavailableException("Service B down", null);
        };

        RetryRegistry retryReg = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(1)
                .waitDuration(Duration.ofMillis(10))
                .retryExceptions(LookupServiceUnavailableException.class)
                .build());

        CircuitBreakerRegistry cbReg = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(2)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .recordExceptions(LookupServiceUnavailableException.class)
                .build());

        ResilientPartLookupDecorator decorator = new ResilientPartLookupDecorator(
                failingDelegate,
                id -> { throw new UnsupportedOperationException(); },
                id -> { throw new UnsupportedOperationException(); },
                retryReg,
                cbReg
        );

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new TestLookupController(decorator))
                .setControllerAdvice(new Lab10ResilienceExceptionHandler())
                .build();

        mvc.perform(get("/test/parts/1/price")).andExpect(status().isServiceUnavailable());
        mvc.perform(get("/test/parts/1/price")).andExpect(status().isServiceUnavailable());

        mvc.perform(get("/test/parts/1/price"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}