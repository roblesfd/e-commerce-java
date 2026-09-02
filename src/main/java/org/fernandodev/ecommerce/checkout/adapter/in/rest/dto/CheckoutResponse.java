package org.fernandodev.ecommerce.checkout.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta con el estado de un checkout.
 */
public record CheckoutResponse(
        UUID id,
        UUID customerId,
        UUID cartId,
        UUID addressId,
        String addressSnapshot,
        List<CheckoutItemResponse> items,
        String couponCode,
        MoneyResponse subtotal,
        MoneyResponse discounts,
        MoneyResponse total,
        String status,
        UUID orderId,
        UUID paymentId,
        Instant createdAt,
        Instant updatedAt
) {

    public record CheckoutItemResponse(
            UUID productId,
            UUID variantId,
            int quantity,
            MoneyResponse unitPrice
    ) {
    }

    public record MoneyResponse(BigDecimal amount, String currency) {
    }
}