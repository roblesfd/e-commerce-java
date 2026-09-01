package org.fernandodev.ecommerce.customer.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Correo electrónico del cliente. Encapsula la validación de formato.
 */
public record Email(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        Objects.requireNonNull(value, "email must not be null");
        value = value.trim();
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Email inválido: " + value);
        }
    }
}