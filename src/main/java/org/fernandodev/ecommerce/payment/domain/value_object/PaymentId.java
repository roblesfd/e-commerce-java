package org.fernandodev.ecommerce.payment.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un pago dentro del contexto Payments.
 */
public record PaymentId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public PaymentId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link PaymentId} con el valor dado.
     */
    public static PaymentId of(UUID value) {
        return new PaymentId(value);
    }
}