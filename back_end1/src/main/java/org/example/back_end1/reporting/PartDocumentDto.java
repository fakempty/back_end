package org.example.back_end1.reporting;

import java.math.BigDecimal;
import java.util.List;

public record PartDocumentDto(
        String documentNumber,
        String documentTitle,
        String generatedAtFormatted,
        String zoneId,
        String customerReference,
        String createdBy,
        List<PartDocumentLineDto> lines,
        BigDecimal subtotalBeforeDiscount,
        BigDecimal totalDiscount,
        BigDecimal subtotalAfterDiscount,
        BigDecimal vatRatePercent,
        BigDecimal vatAmount,
        BigDecimal grandTotal
) {}