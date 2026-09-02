package org.fernandodev.ecommerce.order.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta con el detalle de una orden.
 */
public record OrderResponse(
        UUID id,
        UUID customerId,
        String status,
        List<OrderItemResponse> items,
        UUID addressId,
        String addressSnapshot,
        String couponCode,
        MoneyResponse subtotal,
        MoneyResponse discounts,
        MoneyResponse total,
        List<StatusEntryResponse> statusHistory,
        Instant createdAt
) {

    public record OrderItemResponse(
            UUID id,
            UUID productId,
            UUID variantId,
            String sku,
            String name,
            int quantity,
            MoneyResponse unitPrice,
            MoneyResponse subtotal
    ) {
    }

    public record MoneyResponse(BigDecimal amount, String currency) {
    }

    public record StatusEntryResponse(String status, Instant at) {
    }
}