package org.example.back_end1.batch;

import org.example.back_end1.entity.Part;
import org.example.back_end1.repository.PartRepository;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class PartCsvItemProcessor implements ItemProcessor<PartCsvRow, Part> {

    private final PartRepository partRepository;

    public PartCsvItemProcessor(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    @Override
    public Part process(PartCsvRow row) {
        if (row == null) {
            return null;
        }

        String sku = row.getSku() != null ? row.getSku().trim() : "";
        String name = row.getName() != null ? row.getName().trim() : "";

        if (sku.isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name is required for SKU=" + sku);
        }

        BigDecimal price;
        try {
            price = new BigDecimal(row.getPrice().trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid price '" + row.getPrice() + "' for SKU=" + sku, ex);
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Negative price for SKU=" + sku);
        }

        int quantity;
        try {
            quantity = Integer.parseInt(row.getQuantity().trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid quantity '" + row.getQuantity() + "' for SKU=" + sku, ex);
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Negative quantity for SKU=" + sku);
        }

        Optional<Part> existingOpt = partRepository.findBySku(sku);
        if (existingOpt.isEmpty()) {
            Part created = new Part();
            created.setSku(sku);
            created.setName(name);
            created.setPrice(price);
            created.setQuantity(quantity);
            return created;
        }

        Part existing = existingOpt.get();
        boolean unchanged = Objects.equals(existing.getName(), name)
                && existing.getPrice() != null && existing.getPrice().compareTo(price) == 0
                && Objects.equals(existing.getQuantity(), quantity);

        if (unchanged) {
            // Повертаємо null — Spring Batch зарахує запис як filtered і не передасть у writer
            return null;
        }

        existing.setName(name);
        existing.setPrice(price);
        existing.setQuantity(quantity);
        return existing;
    }
}