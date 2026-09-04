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

    /**
     * Item del carrito en la respuesta.
     *
     * @param id        identificador del item.
     * @param productId producto.
     * @param variantId variante (null si el item es a nivel producto).
     * @param quantity  cantidad.
     * @param unitPrice precio unitario capturado.
     */
    public record CartItemResponse(
            UUID id,
            UUID productId,
            UUID variantId,
            int quantity,
            MoneyResponse unitPrice
    ) {
    }

    /**
     * Cupón aplicado en la respuesta.
     *
     * @param code     código del cupón.
     * @param discount descuento calculado.
     */
    public record AppliedCouponResponse(String code, MoneyResponse discount) {
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