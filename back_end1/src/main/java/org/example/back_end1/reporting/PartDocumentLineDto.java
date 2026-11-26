package org.example.back_end1.reporting;

import java.math.BigDecimal;

public record PartDocumentLineDto(
        int lineNumber,
        String sku,
        String name,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        BigDecimal lineSubtotal,
        BigDecimal lineTotalAfterDiscount
) {}