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

    /**
     * Item de la orden en la respuesta.
     *
     * @param id         identificador del item.
     * @param productId  producto.
     * @param variantId  variante (null si es a nivel producto).
     * @param sku        snapshot del SKU.
     * @param name       snapshot del nombre.
     * @param quantity   cantidad.
     * @param unitPrice  snapshot del precio unitario.
     * @param subtotal   subtotal del item.
     */
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

    /**
     * Importe monetario en la respuesta.
     *
     * @param amount   cantidad.
     * @param currency divisa ISO 4217.
     */
    public record MoneyResponse(BigDecimal amount, String currency) {
    }

    /**
     * Entrada del historial de estados en la respuesta.
     *
     * @param status estado.
     * @param at     momento del cambio.
     */
    public record StatusEntryResponse(String status, Instant at) {
    }
}