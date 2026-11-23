package org.example.back_end2.lookup;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v2/lookup/parts")
public class PartLookupRestController {

    public record PriceDto(long partId, BigDecimal price, String currency) {}
    public record StockDto(long partId, int quantity, String warehouse) {}
    public record AvailabilityDto(long partId, boolean available, String status) {}

    private final PartLookupCatalogService catalog;

    public PartLookupRestController(PartLookupCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/{id}/price")
    public ResponseEntity<PriceDto> getPrice(@PathVariable long id) {
        PartLookupCatalogService.PartRecord item = catalog.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found: " + id));
        return ResponseEntity.ok(new PriceDto(item.id(), item.price(), item.currency()));
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<StockDto> getStock(@PathVariable long id) {
        PartLookupCatalogService.PartRecord item = catalog.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found: " + id));
        return ResponseEntity.ok(new StockDto(item.id(), item.quantity(), item.warehouse()));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityDto> getAvailability(@PathVariable long id) {
        PartLookupCatalogService.PartRecord item = catalog.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found: " + id));
        return ResponseEntity.ok(new AvailabilityDto(item.id(), item.available(), item.status()));
    }
}