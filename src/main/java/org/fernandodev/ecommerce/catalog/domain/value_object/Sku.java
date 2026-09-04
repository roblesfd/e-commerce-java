package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Locale;
import java.util.Objects;

/**
 * Código de unidad de mantenimiento de existencias (SKU) de un producto.
 * <p>
 * Se normaliza a mayúsculas y sin espacios al crear el valor.
 * </p>
 */
public record Sku(String value) {

    /**
     * Constructor compacto que normaliza y valida el SKU.
     *
     * @throws NullPointerException     si {@code value} es {@code null}.
     * @throws IllegalArgumentException si el SKU está vacío o excede 64 caracteres.
     */
    public Sku {
        Objects.requireNonNull(value, "sku no puede ser null");
        value = value.trim().toUpperCase(Locale.ROOT);
        if (value.isBlank()) {
            throw new IllegalArgumentException("sku no puede ser vacío");
        }
        if (value.length() > 64) {
            throw new IllegalArgumentException("sku demasiado largo (máx. 64 caracteres)");
        }
    }
}