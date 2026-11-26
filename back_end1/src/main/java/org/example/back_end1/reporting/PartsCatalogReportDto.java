package org.example.back_end1.reporting;

import java.math.BigDecimal;
import java.util.List;

public record PartsCatalogReportDto(
        String reportTitle,
        String generatedAtFormatted,
        String zoneId,
        int totalItems,
        int maxAllowedItems,
        List<PartsCatalogRowDto> rows,
        BigDecimal totalQuantity,
        BigDecimal totalNetAmount,
        BigDecimal totalVatAmount,
        BigDecimal totalGrossAmount
) {}