package org.fernandodev.ecommerce.cart.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Respuesta con el estado del carrito.
 * <p>
 * Los totales son {@code null} cuando el carrito está vacío (no hay moneda
 * definida).
 * </p>
 */
public record CartResponse(
        UUID id,
        List<CartItemResponse> items,
        AppliedCouponResponse coupon,
        MoneyResponse subtotal,
        MoneyResponse discounts,
        MoneyResponse total
) {

    public record CartItemResponse(
            UUID id,
            UUID productId,
            UUID variantId,
            int quantity,
            MoneyResponse unitPrice
    ) {
    }

    public record AppliedCouponResponse(String code, MoneyResponse discount) {
    }

    public record MoneyResponse(BigDecimal amount, String currency) {
    }
}