package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una variante de producto dentro del agregado Product.
 */
public record ProductVariantId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public ProductVariantId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static ProductVariantId generate() {
        return new ProductVariantId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link ProductVariantId} con el valor dado.
     */
    public static ProductVariantId of(UUID value) {
        return new ProductVariantId(value);
    }
}