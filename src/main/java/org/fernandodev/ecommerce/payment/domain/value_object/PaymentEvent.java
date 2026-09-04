package org.fernandodev.ecommerce.payment.domain.value_object;

import java.time.Instant;
import java.util.Objects;

/**
 * Evento del historial de un pago (cambios de estado y callbacks del proveedor).
 *
 * @param type   tipo de evento.
 * @param detail detalle (opcional).
 * @param at     momento del evento.
 */
public record PaymentEvent(String type, String detail, Instant at) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code type} o {@code at} son {@code null}.
     */
    public PaymentEvent {
        Objects.requireNonNull(type, "type no puede ser null");
        Objects.requireNonNull(at, "at no puede ser null");
    }
}