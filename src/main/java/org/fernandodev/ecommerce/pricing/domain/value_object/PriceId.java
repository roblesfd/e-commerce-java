package org.fernandodev.ecommerce.pricing.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un precio dentro del contexto Pricing.
 */
public record PriceId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public PriceId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static PriceId generate() {
        return new PriceId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link PriceId} con el valor dado.
     */
    public static PriceId of(UUID value) {
        return new PriceId(value);
    }
}