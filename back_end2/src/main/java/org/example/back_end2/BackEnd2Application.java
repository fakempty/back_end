package org.example.back_end2;

import org.example.common.dto.SummaryDto;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BackEnd2Application {

    public static void main(String[] args) {
        SpringApplication.run(BackEnd2Application.class, args);
    }

    @GetMapping("/api/v2/summary")
    public SummaryDto summary() {
        return new SummaryDto("back_end2", 200, "Service 2 running on port 8082");
    }
}