package org.fernandodev.ecommerce.customer.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una dirección del cliente.
 */
public record AddressId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public AddressId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static AddressId generate() {
        return new AddressId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link AddressId} con el valor dado.
     */
    public static AddressId of(UUID value) {
        return new AddressId(value);
    }
}
