package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de categoría dentro del contexto Product Catalog.
 */
public record CategoryId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public CategoryId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link CategoryId} con el valor dado.
     */
    public static CategoryId of(UUID value) {
        return new CategoryId(value);
    }
}