package org.fernandodev.ecommerce.order.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un item de orden.
 */
public record OrderItemId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public OrderItemId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static OrderItemId generate() {
        return new OrderItemId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link OrderItemId} con el valor dado.
     */
    public static OrderItemId of(UUID value) {
        return new OrderItemId(value);
    }
}