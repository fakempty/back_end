package org.example.back_end1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BackEnd1ApplicationCoverageTest {
    @Test
    void contextLoads() {
        BackEnd1Application app = new BackEnd1Application();
        assertNotNull(app);
    }
}