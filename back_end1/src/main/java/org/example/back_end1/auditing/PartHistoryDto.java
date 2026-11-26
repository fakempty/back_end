package org.example.back_end1.auditing;

import org.hibernate.envers.RevisionType;

import java.math.BigDecimal;
import java.time.Instant;

public record PartHistoryDto(
        int revisionNumber,
        RevisionType revisionType,
        Instant revisionTime,
        String actor,
        Long partId,
        String name,
        String sku,
        BigDecimal price,
        Integer quantity,
        String createdBy,
        String updatedBy
) {}