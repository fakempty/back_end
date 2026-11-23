package org.example.back_end2.lookup;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PartLookupCatalogService {

    public record PartRecord(long id, BigDecimal price, String currency, int quantity, String warehouse, boolean available, String status) {}

    private final Map<Long, PartRecord> parts = new ConcurrentHashMap<>();

    public PartLookupCatalogService() {
        parts.put(1L, new PartRecord(1L, new BigDecimal("25.00"), "USD", 12, "WH-MAIN", true, "IN_STOCK"));
        parts.put(2L, new PartRecord(2L, new BigDecimal("140.50"), "USD", 0, "WH-MAIN", false, "OUT_OF_STOCK"));
    }

    public Optional<PartRecord> findById(long id) {
        return Optional.ofNullable(parts.get(id));
    }
}