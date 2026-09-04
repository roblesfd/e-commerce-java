package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;

/**
 * Imagen de un producto (URL).
 */
public record ProductImage(String url) {

    /**
     * Constructor compacto que normaliza y valida la URL.
     *
     * @throws NullPointerException     si {@code url} es {@code null}.
     * @throws IllegalArgumentException si la URL está vacía o excede 500 caracteres.
     */
    public ProductImage {
        Objects.requireNonNull(url, "url no puede ser null");
        url = url.trim();
        if (url.isBlank()) {
            throw new IllegalArgumentException("url de imagen no puede ser vacía");
        }
        if (url.length() > 500) {
            throw new IllegalArgumentException("url de imagen demasiado larga (máx. 500 caracteres)");
        }
    }
}