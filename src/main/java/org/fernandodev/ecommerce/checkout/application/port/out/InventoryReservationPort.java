package org.fernandodev.ecommerce.checkout.application.port.out;

import java.time.Instant;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Puerto de reserva/liberación de inventario (contexto Inventory).
 */
public interface InventoryReservationPort {

    UUID reserve(ProductId productId, ProductVariantId variantId, int quantity, Instant expiresAt);

    void release(ProductId productId, ProductVariantId variantId, UUID reservationId);
}