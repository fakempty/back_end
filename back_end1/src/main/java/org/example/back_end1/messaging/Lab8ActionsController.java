package org.example.back_end1.messaging;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lab8/actions")
public class Lab8ActionsController {

    public record ActionResponse(String status, String action) {}

    private final PartEventPublisher publisher;

    public Lab8ActionsController(PartEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/part-created")
    @Transactional
    public ResponseEntity<ActionResponse> partCreated(@RequestBody Map body) {
        String name = String.valueOf(body.getOrDefault("name", "DefaultPart"));
        String sku = String.valueOf(body.getOrDefault("sku", "SKU-1"));
        BigDecimal price = new BigDecimal(String.valueOf(body.getOrDefault("price", "10.00")));
        Integer qty = Integer.valueOf(String.valueOf(body.getOrDefault("quantity", "1")));
        publisher.publishAfterCommit("PART_CREATED", 1L, name, sku, price, qty);
        return ResponseEntity.ok(new ActionResponse("published", "PART_CREATED"));
    }

    @PostMapping("/quantity-updated")
    @Transactional
    public ResponseEntity<ActionResponse> quantityUpdated(@RequestBody Map body) {
        Long partId = Long.valueOf(String.valueOf(body.getOrDefault("partId", "1")));
        Integer qty = Integer.valueOf(String.valueOf(body.getOrDefault("quantity", "5")));
        publisher.publishAfterCommit("PART_QUANTITY_UPDATED", partId, "UpdatedPart", "SKU-UPD", BigDecimal.ZERO, qty);
        return ResponseEntity.ok(new ActionResponse("published", "PART_QUANTITY_UPDATED"));
    }

    @PostMapping("/report-requested")
    @Transactional
    public ResponseEntity<ActionResponse> reportRequested() {
        publisher.publishAfterCommit("REPORT_REQUESTED", null, "Report", "RPT", BigDecimal.ZERO, 0);
        return ResponseEntity.ok(new ActionResponse("published", "REPORT_REQUESTED"));
    }

    @PostMapping("/failing-action")
    @Transactional
    public ResponseEntity<String> failingAction() {
        publisher.publishAfterCommit("PART_CREATED", 999L, "ShouldNotPublish", "ERR", BigDecimal.ONE, 1);
        throw new IllegalStateException("Simulated rollback");
    }
}