package org.example.back_end1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BackEnd1Application {

    static {
        // PostgreSQL may reject the legacy Europe/Kiev timezone sent by the JDBC driver.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(BackEnd1Application.class, args);
    }
}