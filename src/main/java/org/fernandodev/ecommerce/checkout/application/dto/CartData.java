package org.fernandodev.ecommerce.checkout.application.dto;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Datos del carrito del cliente aportados por el contexto Shopping Cart.
 */
public record CartData(UUID cartId, List<CartItemData> items, AppliedCouponData coupon) {

    /**
     * Item del carrito.
     *
     * @param productId producto.
     * @param variantId variante (null si el item es a nivel producto).
     * @param quantity  cantidad.
     * @param unitPrice precio unitario vigente.
     */
    public record CartItemData(
            ProductId productId,
            ProductVariantId variantId,
            int quantity,
            Money unitPrice
    ) {
    }

    /**
     * Cupón aplicado al carrito.
     *
     * @param code     código del cupón.
     * @param discount descuento calculado.
     */
    public record AppliedCouponData(String code, Money discount) {
    }
}