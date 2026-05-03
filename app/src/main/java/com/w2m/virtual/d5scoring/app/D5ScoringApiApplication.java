package com.w2m.virtual.d5scoring.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque del back D5 Vendor Scoring.
 *
 * <p>Consume eventos de bookings.events vía Kafka y mantiene un score agregado
 * en memoria por supplier. Expone API REST de consulta.</p>
 */
@SpringBootApplication(scanBasePackages = "com.w2m.virtual.d5scoring")
public class D5ScoringApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(D5ScoringApiApplication.class, args);
    }
}
