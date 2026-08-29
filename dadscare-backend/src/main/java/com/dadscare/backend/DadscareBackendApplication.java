package com.dadscare.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** {@code @EnableScheduling} drives VelosyssPollingService's positions/events reconciliation polls. */
@SpringBootApplication
@EnableScheduling
public class DadscareBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DadscareBackendApplication.class, args);
    }
}
