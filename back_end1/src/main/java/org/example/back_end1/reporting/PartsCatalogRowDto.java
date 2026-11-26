package org.example.back_end1.reporting;

import java.math.BigDecimal;

public record PartsCatalogRowDto(
        int index,
        Long id,
        String sku,
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal discountPercent,
        BigDecimal netTotal
) {}