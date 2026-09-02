package org.fernandodev.ecommerce.promotion.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Uso de una promoción por un cliente (para límites de uso por cliente).
 *
 * @param customerId identificador del cliente (usuario IAM).
 * @param count      número de usos registrados.
 */
public record CustomerUsage(UUID customerId, int count) {

    public CustomerUsage {
        Objects.requireNonNull(customerId, "customerId no puede ser null");
        if (count < 0) {
            throw new IllegalArgumentException("count no puede ser negativo");
        }
    }
}