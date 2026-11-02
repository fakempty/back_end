package org.example.back_end1.repository;

import org.example.back_end1.entity.Part;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PartRepositoryIntegrationTest {

    @Autowired
    private PartRepository partRepository;

    @Test
    void testSaveAndFindPart_repositoryState() {
        Part part = new Part();
        part.setName("Test Brake Disc");
        part.setPrice(BigDecimal.valueOf(1500.0));
        part.setQuantity(10);
        part.setSku("SKU-" + UUID.randomUUID().toString().substring(0, 8));

        Part saved = partRepository.save(part);
        assertNotNull(saved.getId());

        Part found = partRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Test Brake Disc", found.getName());
    }

    @Test
    void testDeletePart_repositoryState() {
        Part part = new Part();
        part.setName("Temporary Part");
        part.setPrice(BigDecimal.valueOf(500.0));
        part.setQuantity(5);
        part.setSku("SKU-" + UUID.randomUUID().toString().substring(0, 8));

        Part saved = partRepository.save(part);
        Long id = saved.getId();

        partRepository.deleteById(id);
        boolean exists = partRepository.existsById(id);
        assertFalse(exists);
    }
}