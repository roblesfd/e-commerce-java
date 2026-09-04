package org.fernandodev.ecommerce.checkout.domain.value_object;

import java.util.Objects;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Referencia a una reserva de inventario realizada durante el checkout.
 *
 * @param productId     producto reservado.
 * @param variantId     variante reservada (null si es a nivel producto).
 * @param reservationId identificador de la reserva en el contexto Inventory.
 */
public record ReservationReference(
        ProductId productId,
        ProductVariantId variantId,
        UUID reservationId
) {

    /**
     * Constructor compacto.
     *
     * @throws NullPointerException si {@code productId} o {@code reservationId} son {@code null}.
     */
    public ReservationReference {
        Objects.requireNonNull(productId, "productId no puede ser null");
        Objects.requireNonNull(reservationId, "reservationId no puede ser null");
    }
}