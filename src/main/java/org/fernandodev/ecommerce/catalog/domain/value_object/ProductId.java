package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de producto dentro del contexto Product Catalog.
 */
public record ProductId(UUID value) {

    public ProductId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }

    public static ProductId of(UUID value) {
        return new ProductId(value);
    }
}