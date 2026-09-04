package org.fernandodev.ecommerce.checkout.adapter.out.persistence.redis;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Representación serializable del checkout para persistir en Redis (JSON).
 */
public record CheckoutDocument(
        UUID id,
        UUID customerId,
        String idempotencyKey,
        UUID cartId,
        UUID addressId,
        String addressSnapshot,
        List<Item> items,
        List<Reservation> reservations,
        String couponCode,
        BigDecimal couponAmount,
        String couponCurrency,
        BigDecimal subtotalAmount,
        BigDecimal discountsAmount,
        BigDecimal totalAmount,
        String currency,
        String status,
        UUID orderId,
        UUID paymentId,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Item del checkout serializable.
     *
     * @param productId producto.
     * @param variantId variante (null si el item es a nivel producto).
     * @param quantity  cantidad.
     * @param amount    precio unitario.
     * @param currency  divisa ISO 4217.
     */
    public record Item(UUID productId, UUID variantId, int quantity, BigDecimal amount, String currency) {
    }

    /**
     * Reserva de inventario serializable.
     *
     * @param productId     producto reservado.
     * @param variantId     variante reservada (null si es a nivel producto).
     * @param reservationId identificador de la reserva.
     */
    public record Reservation(UUID productId, UUID variantId, UUID reservationId) {
    }
}