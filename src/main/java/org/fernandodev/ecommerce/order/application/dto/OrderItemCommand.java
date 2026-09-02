package org.fernandodev.ecommerce.order.application.dto;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Item de una orden con snapshot de producto y precio.
 */
public record OrderItemCommand(
        ProductId productId,
        ProductVariantId variantId,
        String sku,
        String name,
        int quantity,
        Money unitPrice
) {
}