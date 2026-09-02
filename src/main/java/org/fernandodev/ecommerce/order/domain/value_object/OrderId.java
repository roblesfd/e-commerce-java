package org.fernandodev.ecommerce.order.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una orden dentro del contexto Order Management.
 */
public record OrderId(UUID value) {

    public OrderId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }
}