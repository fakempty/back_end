package org.example.back_end1.service;

import org.example.back_end1.dto.CreatePartRequest;
import org.example.back_end1.dto.PartDto;
import org.example.back_end1.entity.Part;
import org.example.back_end1.mapper.PartMapper;
import org.example.back_end1.repository.PartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartServiceTest {

    @Mock
    private PartRepository repository;

    @Mock
    private PartMapper mapper;

    @InjectMocks
    private PartService service;

    @Captor
    private ArgumentCaptor<Part> partCaptor;

    // --- 1. getAll() ---

    @Test
    @DisplayName("getAll: повертає список DTO деталей за наявності даних")
    void getAll_whenPartsExist_returnsDtoList() {
        // Arrange
        Part entity = new Part();
        entity.setId(1L);

        PartDto dto = new PartDto();
        dto.setId(1L);
        dto.setName("Колодки");
        dto.setSku("BRK-1");

        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        List<PartDto> result = service.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BRK-1", result.get(0).getSku());
        verify(repository, times(1)).findAll();
        verify(mapper, times(1)).toDto(entity);
    }

    @Test
    @DisplayName("getAll: повертає порожній список, якщо деталей немає")
    void getAll_whenEmpty_returnsEmptyList() {
        // Arrange
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PartDto> result = service.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAll();
        verifyNoInteractions(mapper);
    }

    // --- 2. getById() ---

    @Test
    @DisplayName("getById: повертає DTO деталі за дійсним ID")
    void getById_validId_returnsDto() {
        // Arrange
        Long id = 10L;
        Part entity = new Part();
        entity.setId(id);

        PartDto dto = new PartDto();
        dto.setId(id);
        dto.setName("Фільтр");

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        // Act
        PartDto result = service.getById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Фільтр", result.getName());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("getById: викидає NoSuchElementException, коли деталь відсутня")
    void getById_notFound_throwsNoSuchElementException() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> service.getById(99L));
        verify(repository).findById(99L);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("getById: викидає IllegalArgumentException на невалідний ID")
    void getById_invalidId_throwsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.getById(-1L));
        assertThrows(IllegalArgumentException.class, () -> service.getById(null));
        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }

    // --- 3. create() ---

    @Test
    @DisplayName("create: успішно зберігає деталь і перевіряє параметри через ArgumentCaptor")
    void create_validRequest_savesAndReturnsDto() {
        // Arrange
        CreatePartRequest req = new CreatePartRequest();
        req.setName("Амортизатор");
        req.setSku("SHK-10");
        req.setPrice(new BigDecimal("1200.00"));
        req.setQuantity(5);

        Part savedEntity = new Part();
        savedEntity.setId(1L);

        PartDto expectedDto = new PartDto();
        expectedDto.setId(1L);
        expectedDto.setName("Амортизатор");
        expectedDto.setSku("SHK-10");

        when(repository.save(any(Part.class))).thenReturn(savedEntity);
        when(mapper.toDto(savedEntity)).thenReturn(expectedDto);

        // Act
        PartDto result = service.create(req);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).save(partCaptor.capture());
        Part captured = partCaptor.getValue();
        assertEquals("Амортизатор", captured.getName());
        assertEquals("SHK-10", captured.getSku());
    }

    @Test
    @DisplayName("create: викидає виняток, якщо запит null або поля порожні")
    void create_invalidRequest_throwsIllegalArgumentException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.create(null));

        CreatePartRequest badName = new CreatePartRequest();
        badName.setName(" ");
        badName.setSku("SKU-1");
        assertThrows(IllegalArgumentException.class, () -> service.create(badName));

        CreatePartRequest badPrice = new CreatePartRequest();
        badPrice.setName("Деталь");
        badPrice.setSku("SKU-2");
        badPrice.setPrice(new BigDecimal("-1.00"));
        assertThrows(IllegalArgumentException.class, () -> service.create(badPrice));

        verify(repository, never()).save(any());
    }

    // --- 4. update() ---

    @Test
    @DisplayName("update: успішно оновлює існуючу деталь")
    void update_validRequest_updatesPart() {
        // Arrange
        Part existing = new Part();
        existing.setId(2L);

        CreatePartRequest req = new CreatePartRequest();
        req.setName("Нова назва");
        req.setSku("NEW-1");
        req.setPrice(new BigDecimal("500.00"));
        req.setQuantity(8);

        Part updatedEntity = new Part();
        updatedEntity.setId(2L);

        PartDto expectedDto = new PartDto();
        expectedDto.setId(2L);
        expectedDto.setName("Нова назва");

        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Part.class))).thenReturn(updatedEntity);
        when(mapper.toDto(updatedEntity)).thenReturn(expectedDto);

        // Act
        PartDto result = service.update(2L, req);

        // Assert
        assertNotNull(result);
        assertEquals("Нова назва", result.getName());
        verify(repository).save(partCaptor.capture());
        assertEquals("Нова назва", partCaptor.getValue().getName());
    }

    @Test
    @DisplayName("update: викидає NoSuchElementException, якщо ID не знайдено")
    void update_notFound_throwsNoSuchElementException() {
        // Arrange
        CreatePartRequest req = new CreatePartRequest();
        req.setName("Тест");
        req.setSku("TST-1");
        req.setPrice(new BigDecimal("100.00"));
        req.setQuantity(1);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> service.update(99L, req));
        verify(repository, never()).save(any());
    }

    // --- 5. delete() ---

    @Test
    @DisplayName("delete: успішно видаляє деталь за наявним ID")
    void delete_existingId_deletesSuccessfully() {
        // Arrange
        when(repository.existsById(5L)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> service.delete(5L));

        // Assert
        verify(repository).existsById(5L);
        verify(repository).deleteById(5L);
    }

    @Test
    @DisplayName("delete: викидає NoSuchElementException, якщо деталь не існує")
    void delete_notFound_throwsNoSuchElementException() {
        // Arrange
        when(repository.existsById(404L)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> service.delete(404L));
        verify(repository).existsById(404L);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete: викидає IllegalArgumentException на некоректний ID")
    void delete_invalidId_throwsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.delete(-10L));
        assertThrows(IllegalArgumentException.class, () -> service.delete(null));
        verifyNoInteractions(repository);
    }
}