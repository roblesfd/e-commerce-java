package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de producto dentro del contexto Product Catalog.
 */
public record ProductId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public ProductId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link ProductId} con el valor dado.
     */
    public static ProductId of(UUID value) {
        return new ProductId(value);
    }
}