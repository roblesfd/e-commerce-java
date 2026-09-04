package org.fernandodev.ecommerce.pricing.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un override (precio promocional o temporal) dentro del
 * agregado Price.
 */
public record PriceOverrideId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public PriceOverrideId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static PriceOverrideId generate() {
        return new PriceOverrideId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link PriceOverrideId} con el valor dado.
     */
    public static PriceOverrideId of(UUID value) {
        return new PriceOverrideId(value);
    }
}