package org.fernandodev.ecommerce.catalog.domain.value_object;

import java.util.Objects;

/**
 * Atributo de producto o variante en forma de par nombre/valor.
 */
public record ProductAttribute(String name, String value) {

    public ProductAttribute {
        Objects.requireNonNull(name, "name no puede ser null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name de atributo no puede ser vacío");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("name de atributo demasiado largo (máx. 100 caracteres)");
        }
        Objects.requireNonNull(value, "value no puede ser null");
        if (value.length() > 500) {
            throw new IllegalArgumentException("value de atributo demasiado largo (máx. 500 caracteres)");
        }
    }
}