package org.example.back_end2.messaging;

import org.example.common.dto.PartEventDto;
import org.example.common.dto.SummaryDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RestController
public class PartEventConsumer {

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();
    private final Map<String, AtomicInteger> eventCounts = new ConcurrentHashMap<>();

    public void onEvent(PartEventDto event) {
        if (event == null || event.eventId() == null) {
            return;
        }
        // Ідемпотентність: якщо eventId вже оброблено, ігноруємо дублікат
        if (!processedEventIds.add(event.eventId())) {
            return;
        }
        eventCounts.computeIfAbsent(event.eventType(), k -> new AtomicInteger(0)).incrementAndGet();
    }

    @RabbitListener(queues = RabbitMqConfig.PARTS_QUEUE)
    public void receive(PartEventDto event) {
        onEvent(event);
    }

    public int getCount(String eventType) {
        return eventCounts.getOrDefault(eventType, new AtomicInteger(0)).get();
    }

    @GetMapping("/api/v2/events/summary")
    public SummaryDto eventsSummary() {
        int total = eventCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        return new SummaryDto("back_end2", 200, "Processed unique events: " + total + ", counts: " + eventCounts);
    }
}