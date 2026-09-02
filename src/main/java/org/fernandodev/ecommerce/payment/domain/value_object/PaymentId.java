package org.fernandodev.ecommerce.payment.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un pago dentro del contexto Payments.
 */
public record PaymentId(UUID value) {

    public PaymentId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID());
    }

    public static PaymentId of(UUID value) {
        return new PaymentId(value);
    }
}