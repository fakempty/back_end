package org.example.back_end1.service;

import org.example.back_end1.aop.LogExecutionTime;
import org.example.back_end1.dto.CreatePartRequest;
import org.example.back_end1.dto.PartDto;
import org.example.back_end1.entity.Part;
import org.example.back_end1.mapper.PartMapper;
import org.example.back_end1.repository.PartRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PartService {

    private final PartRepository repository;
    private final PartMapper mapper;

    public PartService(PartRepository repository, PartMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @LogExecutionTime
    public List<PartDto> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public PartDto getById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID деталі має бути додатним числом");
        }
        Part part = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Деталь з ID " + id + " не знайдена"));
        return mapper.toDto(part);
    }

    public PartDto create(CreatePartRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Запит не може бути null");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Назва не може бути порожньою");
        }
        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new IllegalArgumentException("Артикул (SKU) не може бути порожнім");
        }
        if (request.getPrice() == null || request.getPrice().doubleValue() < 0) {
            throw new IllegalArgumentException("Ціна не може бути від'ємною");
        }
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new IllegalArgumentException("Кількість не може бути від'ємною");
        }

        Part part = new Part();
        part.setName(request.getName());
        part.setSku(request.getSku());
        part.setPrice(request.getPrice());
        part.setQuantity(request.getQuantity());

        Part saved = repository.save(part);
        return mapper.toDto(saved);
    }

    public PartDto update(Long id, CreatePartRequest request) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID деталі має бути додатним числом");
        }
        if (request == null) {
            throw new IllegalArgumentException("Запит не може бути null");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Назва не може бути порожньою");
        }
        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new IllegalArgumentException("Артикул (SKU) не може бути порожнім");
        }
        if (request.getPrice() == null || request.getPrice().doubleValue() < 0) {
            throw new IllegalArgumentException("Ціна не може бути від'ємною");
        }
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new IllegalArgumentException("Кількість не може бути від'ємною");
        }

        Part part = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Деталь з ID " + id + " не знайдена"));

        part.setName(request.getName());
        part.setSku(request.getSku());
        part.setPrice(request.getPrice());
        part.setQuantity(request.getQuantity());

        Part updated = repository.save(part);
        return mapper.toDto(updated);
    }

    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID деталі має бути додатним числом");
        }
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Неможливо видалити: деталь з ID " + id + " не існує");
        }
        repository.deleteById(id);
    }
}