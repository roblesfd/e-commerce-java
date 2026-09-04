package org.fernandodev.ecommerce.customer.domain.value_object;

import java.util.Currency;
import java.util.Objects;

/**
 * Preferencias comerciales del cliente.
 *
 * @param newsletterEnabled      si desea recibir novedades.
 * @param preferredCurrencyCode  divisa preferida (ISO 4217).
 */
public record CustomerPreferences(boolean newsletterEnabled, String preferredCurrencyCode) {

    /**
     * Constructor compacto que valida la divisa preferida.
     *
     * @throws NullPointerException     si {@code preferredCurrencyCode} es {@code null}.
     * @throws IllegalArgumentException si la divisa no es un código ISO 4217 válido.
     */
    public CustomerPreferences {
        Objects.requireNonNull(preferredCurrencyCode, "preferredCurrencyCode no puede ser null");
        Currency.getInstance(preferredCurrencyCode);
    }

    /**
     * @return preferencias por defecto (sin novedades y divisa {@code USD}).
     */
    public static CustomerPreferences defaults() {
        return new CustomerPreferences(false, "USD");
    }
}
