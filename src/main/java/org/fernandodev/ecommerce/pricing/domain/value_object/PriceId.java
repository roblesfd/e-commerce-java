package org.fernandodev.ecommerce.pricing.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un precio dentro del contexto Pricing.
 */
public record PriceId(UUID value) {

    public PriceId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static PriceId generate() {
        return new PriceId(UUID.randomUUID());
    }

    public static PriceId of(UUID value) {
        return new PriceId(value);
    }
}