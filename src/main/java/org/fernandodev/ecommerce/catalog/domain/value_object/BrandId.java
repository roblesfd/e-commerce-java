package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de marca dentro del contexto Product Catalog.
 */
public record BrandId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public BrandId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static BrandId generate() {
        return new BrandId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link BrandId} con el valor dado.
     */
    public static BrandId of(UUID value) {
        return new BrandId(value);
    }
}