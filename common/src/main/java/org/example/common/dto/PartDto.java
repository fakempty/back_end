package org.example.common.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PartDto {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer quantity;
}