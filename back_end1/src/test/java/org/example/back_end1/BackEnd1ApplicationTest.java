package org.example.back_end1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BackEnd1ApplicationTest {

    @Test
    @DisplayName("Перевірка завантаження основного класу застосунку")
    void applicationClassLoads() {
        BackEnd1Application app = new BackEnd1Application();
        assertNotNull(app);
    }
}
