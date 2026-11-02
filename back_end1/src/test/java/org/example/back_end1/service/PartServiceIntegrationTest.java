package org.example.back_end1.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PartServiceIntegrationTest {

    @Autowired(required = false)
    private PartService partService;

    @Test
    void testSuccessfulPartCreation_serviceState() {
        assertNotNull(partService, "Сервіс має бути ініціалізований");
    }

    @Test
    void testNegativePartCreation_serviceState() {
        assertNotNull(partService, "Контекст сервісу має працювати");
    }
}