package org.fernandodev.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase de arranque del backend de E-Commerce.
 * <p>
 * Aplicación modular monolith con arquitectura hexagonal; cada bounded
 * context vive en su propio paquete bajo {@code org.fernandodev.ecommerce}.
 * </p>
 */
@SpringBootApplication
public class ECommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceApplication.class, args);
    }
}