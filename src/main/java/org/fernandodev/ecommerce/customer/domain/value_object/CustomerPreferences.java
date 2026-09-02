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

    public CustomerPreferences {
        Objects.requireNonNull(preferredCurrencyCode, "preferredCurrencyCode no puede ser null");
        Currency.getInstance(preferredCurrencyCode);
    }

    public static CustomerPreferences defaults() {
        return new CustomerPreferences(false, "USD");
    }
}
