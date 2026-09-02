package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;

/**
 * Imagen de un producto (URL).
 */
public record ProductImage(String url) {

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