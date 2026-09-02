package org.fernandodev.ecommerce.customer.domain.value_object;

import java.util.Objects;

/**
 * Dirección del cliente. Value object inmutable que encapsula sus invariantes
 * (campos obligatorios no vacíos).
 *
 * @param id         identificador de la dirección.
 * @param street     calle y número.
 * @param city       ciudad.
 * @param state      estado o provincia (opcional).
 * @param postalCode código postal.
 * @param country    país.
 */
public record Address(
        AddressId id,
        String street,
        String city,
        String state,
        String postalCode,
        String country
) {

    public Address {
        Objects.requireNonNull(id, "id no puede ser null");
        requireNotBlank(street, "street");
        requireNotBlank(city, "city");
        requireNotBlank(postalCode, "postalCode");
        requireNotBlank(country, "country");
    }

    private static void requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " no puede ser null");
        }
    }
}
