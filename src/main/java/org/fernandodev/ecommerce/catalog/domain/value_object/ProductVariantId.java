package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una variante de producto dentro del agregado Product.
 */
public record ProductVariantId(UUID value) {

    public ProductVariantId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static ProductVariantId generate() {
        return new ProductVariantId(UUID.randomUUID());
    }

    public static ProductVariantId of(UUID value) {
        return new ProductVariantId(value);
    }
}