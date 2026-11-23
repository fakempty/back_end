package org.example.back_end1.lookup;

import java.math.BigDecimal;

public interface PartPriceLookup {
    record PriceResult(long partId, BigDecimal price, String currency) {}
    PriceResult lookupPrice(long partId);
}