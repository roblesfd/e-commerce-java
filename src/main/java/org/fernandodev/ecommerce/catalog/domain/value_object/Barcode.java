package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;

/**
 * Código de barras de un producto o variante (opcional).
 */
public record Barcode(String value) {

    public Barcode {
        Objects.requireNonNull(value, "barcode no puede ser null");
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("barcode no puede ser vacío");
        }
        if (value.length() > 64) {
            throw new IllegalArgumentException("barcode demasiado largo (máx. 64 caracteres)");
        }
    }
}