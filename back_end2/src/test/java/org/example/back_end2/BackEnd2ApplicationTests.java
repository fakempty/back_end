package org.example.back_end2;

import org.example.common.dto.SummaryDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BackEnd2ApplicationTests {

    @Test
    void contextLoadsAndSummaryWorks() {
        BackEnd2Application app = new BackEnd2Application();
        SummaryDto dto = app.summary();
        assertEquals("back_end2", dto.serviceName());
        assertEquals(200, dto.statusCode());
    }
}