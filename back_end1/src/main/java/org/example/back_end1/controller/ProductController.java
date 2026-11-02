package org.example.back_end1.controller;

import jakarta.validation.Valid;
import org.example.back_end1.dto.ProductDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody @Valid ProductDto productDto) {
        return ResponseEntity.ok("успішно створено товар з кількістю: " + productDto.getQuantity());
    }
}