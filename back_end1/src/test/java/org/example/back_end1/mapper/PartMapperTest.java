package org.example.back_end1.mapper;

import org.example.back_end1.dto.CreatePartRequest;
import org.example.back_end1.dto.PartDto;
import org.example.back_end1.entity.Part;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PartMapperTest {

    private final PartMapper mapper = Mappers.getMapper(PartMapper.class);

    @Test
    @DisplayName("toDto: конвертує Entity в DTO")
    void toDto_convertsCorrectly() {
        Part part = new Part();
        part.setId(1L);
        part.setName("Свічка");
        part.setSku("SPK-01");
        part.setPrice(new BigDecimal("150.00"));
        part.setQuantity(20);

        PartDto dto = mapper.toDto(part);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Свічка", dto.getName());
        assertEquals("SPK-01", dto.getSku());
    }

    @Test
    @DisplayName("toEntity: конвертує CreatePartRequest в Entity")
    void toEntity_convertsCorrectly() {
        CreatePartRequest req = new CreatePartRequest("Фільтр", "FLT-02", new BigDecimal("350.00"), 5);

        Part part = mapper.toEntity(req);

        assertNotNull(part);
        assertEquals("Фільтр", part.getName());
        assertEquals("FLT-02", part.getSku());
        assertEquals(new BigDecimal("350.00"), part.getPrice());
        assertEquals(5, part.getQuantity());
    }
}