package io.github.dokkaltek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A main class for integration tests.
 */
@SpringBootApplication
public class TestIntegrationRunner {
    public static void main(String[] args) {
        SpringApplication.run(TestIntegrationRunner.class, args);
    }
}
