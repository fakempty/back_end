package org.example.back_end1.messaging;

import org.example.common.dto.PartEventDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class PartEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public PartEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishAfterCommit(String eventType, Long partId, String name, String sku, BigDecimal price, Integer quantity) {
        PartEventDto event = new PartEventDto(
                UUID.randomUUID().toString(),
                eventType,
                partId,
                name,
                sku,
                price,
                quantity,
                Instant.now()
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbitTemplate.convertAndSend(RabbitMqConfig.PARTS_QUEUE, event);
                }
            });
        } else {
            rabbitTemplate.convertAndSend(RabbitMqConfig.PARTS_QUEUE, event);
        }
    }
}