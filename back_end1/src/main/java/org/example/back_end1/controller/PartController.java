package org.example.back_end1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.back_end1.dto.CreatePartRequest;
import org.example.back_end1.dto.PartDto;
import org.example.back_end1.service.PartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService service;

    @GetMapping
    public List<PartDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public PartDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // Критерій 7: захист методом @PreAuthorize
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // @PreAuthorize")
    public PartDto create(@Valid @RequestBody CreatePartRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    // @PreAuthorize")
    public PartDto update(@PathVariable Long id, @Valid @RequestBody CreatePartRequest request) {
        return service.update(id, request);
    }

    // Критерій 6: видалення буде обмежено через SecurityFilterChain
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}