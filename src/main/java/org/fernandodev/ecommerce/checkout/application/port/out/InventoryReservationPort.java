package org.fernandodev.ecommerce.checkout.application.port.out;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Puerto de reserva/liberación de inventario (contexto Inventory).
 */
public interface InventoryReservationPort {

/**
     * Reserva stock del producto/variante.
     *
     * @param productId producto.
     * @param variantId variante (null si es a nivel producto).
     * @param quantity  cantidad a reservar.
     * @param expiresAt momento de vencimiento de la reserva.
     * @return el identificador de la reserva.
     */
    UUID reserve(ProductId productId, ProductVariantId variantId, int quantity, Instant expiresAt);

/**
     * Libera una reserva de stock.
     *
     * @param productId     producto.
     * @param variantId     variante (null si es a nivel producto).
     * @param reservationId identificador de la reserva.
     */
    void release(ProductId productId, ProductVariantId variantId, UUID reservationId);
}