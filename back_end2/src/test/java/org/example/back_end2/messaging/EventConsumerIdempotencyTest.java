package org.example.back_end2.messaging;

import org.example.common.dto.PartEventDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventConsumerIdempotencyTest {

    @Test
    void duplicateEventDoesNotChangeResult() {
        PartEventConsumer consumer = new PartEventConsumer();
        PartEventDto event = new PartEventDto(
                "evt-123",
                "PART_CREATED",
                10L,
                "Bolt",
                "SKU-10",
                new BigDecimal("5.00"),
                3,
                Instant.now()
        );

        consumer.onEvent(event);
        consumer.onEvent(event);

        assertEquals(1, consumer.getCount("PART_CREATED"));
    }
}