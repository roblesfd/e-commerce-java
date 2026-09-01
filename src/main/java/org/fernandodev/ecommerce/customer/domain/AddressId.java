package org.fernandodev.ecommerce.customer.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una dirección del cliente.
 */
public record AddressId(UUID value) {

    public AddressId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AddressId generate() {
        return new AddressId(UUID.randomUUID());
    }

    public static AddressId of(UUID value) {
        return new AddressId(value);
    }
}