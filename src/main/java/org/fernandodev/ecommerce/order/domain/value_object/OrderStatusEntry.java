package org.fernandodev.ecommerce.order.domain.value_object;

import java.time.Instant;
import java.util.Objects;

/**
 * Entrada del historial de estados de una orden.
 *
 * @param status estado registrado.
 * @param at     momento del cambio.
 */
public record OrderStatusEntry(OrderStatus status, Instant at) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code status} o {@code at} son {@code null}.
     */
    public OrderStatusEntry {
        Objects.requireNonNull(status, "status no puede ser null");
        Objects.requireNonNull(at, "at no puede ser null");
    }
}