package org.example.back_end1.lookup;

public interface PartStockLookup {
    record StockResult(long partId, int quantity, String warehouse) {}
    StockResult lookupStock(long partId);
}