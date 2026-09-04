package org.fernandodev.ecommerce.checkout.application.dto;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Datos para crear una orden en el contexto Order Management.
 */
public record OrderData(
        UUID customerId,
        UUID addressId,
        String addressSnapshot,
        String couponCode,
        List<OrderItemData> items,
        Money subtotal,
        Money discounts,
        Money total
) {

    /**
     * Item de la orden a crear.
     *
     * @param productId producto.
     * @param variantId variante (null si el item es a nivel producto).
     * @param sku       snapshot del SKU.
     * @param name      snapshot del nombre.
     * @param quantity  cantidad.
     * @param unitPrice snapshot del precio unitario.
     */
    public record OrderItemData(
            ProductId productId,
            ProductVariantId variantId,
            String sku,
            String name,
            int quantity,
            Money unitPrice
    ) {
    }
}