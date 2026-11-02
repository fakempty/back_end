package org.example.back_end1.dto;

import org.example.back_end1.annotation.ValidPartPricing;
import java.math.BigDecimal;

@ValidPartPricing
public class CreatePartRequest {
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer quantity;

    public CreatePartRequest() {}

    public CreatePartRequest(String name, String sku, BigDecimal price, Integer quantity) {
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}