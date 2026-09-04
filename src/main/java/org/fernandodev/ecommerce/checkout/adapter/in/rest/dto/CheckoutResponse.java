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

    /**
     * Item del checkout en la respuesta.
     *
     * @param productId producto.
     * @param variantId variante (null si el item es a nivel producto).
     * @param quantity  cantidad.
     * @param unitPrice precio unitario vigente.
     */
    public record CheckoutItemResponse(
            UUID productId,
            UUID variantId,
            int quantity,
            MoneyResponse unitPrice
    ) {
    }

    /**
     * Importe monetario en la respuesta.
     *
     * @param amount   cantidad.
     * @param currency divisa ISO 4217.
     */
    public record MoneyResponse(BigDecimal amount, String currency) {
    }
}