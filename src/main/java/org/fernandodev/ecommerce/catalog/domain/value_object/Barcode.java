package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;

/**
 * Código de barras de un producto o variante (opcional).
 */
public record Barcode(String value) {

    /**
     * Constructor compacto que normaliza y valida el barcode.
     *
     * @throws NullPointerException     si {@code value} es {@code null}.
     * @throws IllegalArgumentException si el barcode está vacío o excede 64 caracteres.
     */
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