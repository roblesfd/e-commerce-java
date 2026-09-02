package org.fernandodev.ecommerce.cart.application.dto;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Item del carrito aportado al proveedor de cupones para evaluar promociones.
 */
public record CouponItemDto(ProductId productId, int quantity, Money unitPrice) {
}