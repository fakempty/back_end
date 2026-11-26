package org.example.back_end1.reporting;

import org.example.back_end1.entity.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportingCalculationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z");

    private final ZoneId zoneId;
    private final BigDecimal vatRate;

    public ReportingCalculationService(
            @Value("${lab12.reporting.zone-id:Europe/Kyiv}") String zoneIdStr,
            @Value("${lab12.reporting.vat-rate:0.20}") BigDecimal vatRate) {
        this.zoneId = ZoneId.of(zoneIdStr);
        this.vatRate = vatRate;
    }

    public String formatNowInZone() {
        ZonedDateTime now = Instant.now().atZone(zoneId);
        return now.format(DATE_TIME_FORMATTER);
    }

    public String getZoneIdString() {
        return zoneId.getId();
    }

    public BigDecimal resolveDiscountPercent(int quantity, BigDecimal unitPrice) {
        BigDecimal safeQty = BigDecimal.valueOf(Math.max(0, quantity));
        BigDecimal safePrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        BigDecimal grossLine = safePrice.multiply(safeQty);

        if (quantity >= 50 || grossLine.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return BigDecimal.valueOf(10);
        }
        if (quantity >= 20) {
            return BigDecimal.valueOf(5);
        }
        return BigDecimal.ZERO;
    }

    public PartDocumentDto buildSinglePartDocument(Part part, String idempotencyKey) {
        int qty = part.getQuantity() != null ? Math.max(0, part.getQuantity()) : 0;
        BigDecimal price = (part.getPrice() != null ? part.getPrice() : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountPct = resolveDiscountPercent(qty, price);

        BigDecimal lineSubtotal = price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = lineSubtotal
                .multiply(discountPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal lineAfterDiscount = lineSubtotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        PartDocumentLineDto line = new PartDocumentLineDto(
                1,
                part.getSku(),
                part.getName(),
                qty,
                price,
                discountPct,
                discountAmount,
                lineSubtotal,
                lineAfterDiscount
        );

        BigDecimal vatAmount = lineAfterDiscount.multiply(vatRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = lineAfterDiscount.add(vatAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal vatRatePercent = vatRate.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);

        String docNumber = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? "DOC-IDEMP-" + idempotencyKey.trim()
                : "DOC-PART-" + part.getId();

        String customerRef = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? "Ідемпотентний експорт (ключ: " + idempotencyKey.trim() + ")"
                : "Клієнтське підтвердження позиції #" + part.getId();

        return new PartDocumentDto(
                docNumber,
                "Підтвердження замовлення / Рахунок на запчастину",
                formatNowInZone(),
                zoneId.getId(),
                customerRef,
                part.getCreatedBy() != null ? part.getCreatedBy() : "SYSTEM",
                List.of(line),
                lineSubtotal,
                discountAmount,
                lineAfterDiscount,
                vatRatePercent,
                vatAmount,
                grandTotal
        );
    }

    public PartsCatalogReportDto buildAdminCatalogReport(List<Part> parts, int maxAllowedItems) {
        List<PartsCatalogRowDto> rows = new ArrayList<>(parts.size());
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        int idx = 1;
        for (Part part : parts) {
            int qty = part.getQuantity() != null ? Math.max(0, part.getQuantity()) : 0;
            BigDecimal price = (part.getPrice() != null ? part.getPrice() : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discountPct = resolveDiscountPercent(qty, price);
            BigDecimal gross = price.multiply(BigDecimal.valueOf(qty));
            BigDecimal disc = gross.multiply(discountPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal net = gross.subtract(disc).setScale(2, RoundingMode.HALF_UP);

            rows.add(new PartsCatalogRowDto(
                    idx++,
                    part.getId(),
                    part.getSku(),
                    part.getName(),
                    qty,
                    price,
                    discountPct,
                    net
            ));

            totalQty = totalQty.add(BigDecimal.valueOf(qty));
            totalNet = totalNet.add(net);
        }

        BigDecimal roundedNet = totalNet.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalVat = roundedNet.multiply(vatRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalGross = roundedNet.add(totalVat).setScale(2, RoundingMode.HALF_UP);

        return new PartsCatalogReportDto(
                "Адміністративний звіт по номенклатурі запчастин",
                formatNowInZone(),
                zoneId.getId(),
                rows.size(),
                maxAllowedItems,
                rows,
                totalQty,
                roundedNet,
                totalVat,
                totalGross
        );
    }
}