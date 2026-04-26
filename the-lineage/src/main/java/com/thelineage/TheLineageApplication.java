package com.thelineage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TheLineageApplication {

    public static void main(String[] args) {
        SpringApplication.run(TheLineageApplication.class, args);
    }
}
