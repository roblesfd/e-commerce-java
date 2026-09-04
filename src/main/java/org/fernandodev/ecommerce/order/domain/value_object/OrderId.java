package org.fernandodev.ecommerce.order.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una orden dentro del contexto Order Management.
 */
public record OrderId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public OrderId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link OrderId} con el valor dado.
     */
    public static OrderId of(UUID value) {
        return new OrderId(value);
    }
}