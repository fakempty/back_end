package org.example.back_end1.dto;

import org.example.back_end1.annotation.PositiveQuantity;

public class ProductDto {
    private String name;

    @PositiveQuantity
    private Integer quantity;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}