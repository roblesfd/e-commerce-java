package org.fernandodev.ecommerce.checkout.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un checkout dentro del contexto Checkout.
 */
public record CheckoutId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public CheckoutId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static CheckoutId generate() {
        return new CheckoutId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link CheckoutId} con el valor dado.
     */
    public static CheckoutId of(UUID value) {
        return new CheckoutId(value);
    }
}