package org.fernandodev.ecommerce.pricing.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un override (precio promocional o temporal) dentro del
 * agregado Price.
 */
public record PriceOverrideId(UUID value) {

    public PriceOverrideId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static PriceOverrideId generate() {
        return new PriceOverrideId(UUID.randomUUID());
    }

    public static PriceOverrideId of(UUID value) {
        return new PriceOverrideId(value);
    }
}