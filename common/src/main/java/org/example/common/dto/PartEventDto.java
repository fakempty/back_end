package org.example.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PartEventDto(
        String eventId,
        String eventType,
        Long partId,
        String name,
        String sku,
        BigDecimal price,
        Integer quantity,
        Instant occurredAt
) {}