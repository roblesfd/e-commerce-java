package org.fernandodev.ecommerce.inventory.domain.value_object;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una reserva de stock dentro del agregado Inventory.
 */
public record StockReservationId(UUID value) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public StockReservationId {
        Objects.requireNonNull(value, "value no puede ser null");
    }

    /**
     * @return un nuevo identificador aleatorio.
     */
    public static StockReservationId generate() {
        return new StockReservationId(UUID.randomUUID());
    }

    /**
     * @param value identificador.
     * @return un {@link StockReservationId} con el valor dado.
     */
    public static StockReservationId of(UUID value) {
        return new StockReservationId(value);
    }
}