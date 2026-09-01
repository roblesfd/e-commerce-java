package org.fernandodev.ecommerce.customer.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Número de teléfono del cliente. Encapsula la validación de formato.
 */
public record PhoneNumber(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[+0-9 ()-]{7,20}$");

    public PhoneNumber {
        Objects.requireNonNull(value, "phone must not be null");
        value = value.trim();
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Teléfono inválido: " + value);
        }
    }
}