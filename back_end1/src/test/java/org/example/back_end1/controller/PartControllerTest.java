package org.example.back_end1.controller;

import org.example.back_end1.dto.CreatePartRequest;
import org.example.back_end1.dto.PartDto;
import org.example.back_end1.service.PartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartControllerTest {

    @Mock
    private PartService service;

    @InjectMocks
    private PartController controller;

    @Test
    @DisplayName("getAll: повертає список DTO деталей")
    void getAll_returnsList() {
        PartDto dto = new PartDto();
        dto.setId(1L);
        when(service.getAll()).thenReturn(List.of(dto));

        List<PartDto> result = controller.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(service).getAll();
    }

    @Test
    @DisplayName("getById: повертає DTO деталі за ID")
    void getById_returnsPart() {
        PartDto dto = new PartDto();
        dto.setId(5L);
        when(service.getById(5L)).thenReturn(dto);

        PartDto result = controller.getById(5L);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        verify(service).getById(5L);
    }

    @Test
    @DisplayName("create: повертає створену деталь")
    void create_returnsCreatedPart() {
        CreatePartRequest req = new CreatePartRequest();
        PartDto dto = new PartDto();
        dto.setId(10L);

        when(service.create(req)).thenReturn(dto);

        PartDto result = controller.create(req);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(service).create(req);
    }

    @Test
    @DisplayName("update: повертає оновлену деталь")
    void update_returnsUpdatedPart() {
        CreatePartRequest req = new CreatePartRequest();
        PartDto dto = new PartDto();
        dto.setId(2L);

        when(service.update(2L, req)).thenReturn(dto);

        PartDto result = controller.update(2L, req);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(service).update(2L, req);
    }

    @Test
    @DisplayName("delete: успішно викликає видалення у сервісі")
    void delete_callsServiceDelete() {
        doNothing().when(service).delete(1L);

        assertDoesNotThrow(() -> controller.delete(1L));

        verify(service).delete(1L);
    }
}