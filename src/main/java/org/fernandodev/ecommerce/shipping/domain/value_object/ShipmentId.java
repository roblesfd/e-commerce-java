package org.fernandodev.ecommerce.shipping.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un envío.
 */
public record ShipmentId(UUID value) {

    public ShipmentId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static ShipmentId generate() {
        return new ShipmentId(UUID.randomUUID());
    }

    public static ShipmentId of(UUID value) {
        return new ShipmentId(value);
    }
}