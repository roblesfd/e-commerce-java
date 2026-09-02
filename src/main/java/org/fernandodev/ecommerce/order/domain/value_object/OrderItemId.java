package org.fernandodev.ecommerce.order.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un item de orden.
 */
public record OrderItemId(UUID value) {

    public OrderItemId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static OrderItemId generate() {
        return new OrderItemId(UUID.randomUUID());
    }

    public static OrderItemId of(UUID value) {
        return new OrderItemId(value);
    }
}