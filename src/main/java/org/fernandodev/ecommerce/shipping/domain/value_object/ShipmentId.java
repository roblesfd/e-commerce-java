package org.fernandodev.ecommerce.shipping.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un envío.
 */
public record ShipmentId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public ShipmentId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static ShipmentId generate() {
        return new ShipmentId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link ShipmentId} con el valor dado.
     */
    public static ShipmentId of(UUID value) {
        return new ShipmentId(value);
    }
}