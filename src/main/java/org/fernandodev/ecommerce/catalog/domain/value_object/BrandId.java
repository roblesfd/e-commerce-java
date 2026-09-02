package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de marca dentro del contexto Product Catalog.
 */
public record BrandId(UUID value) {

    public BrandId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static BrandId generate() {
        return new BrandId(UUID.randomUUID());
    }

    public static BrandId of(UUID value) {
        return new BrandId(value);
    }
}