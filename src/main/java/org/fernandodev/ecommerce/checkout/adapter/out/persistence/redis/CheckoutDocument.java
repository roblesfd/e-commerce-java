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

    public record Item(UUID productId, UUID variantId, int quantity, BigDecimal amount, String currency) {
    }

    public record Reservation(UUID productId, UUID variantId, UUID reservationId) {
    }
}