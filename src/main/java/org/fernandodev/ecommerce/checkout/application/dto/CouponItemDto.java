package org.fernandodev.ecommerce.checkout.application.dto;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Item del checkout aportado al proveedor de cupones.
 */
public record CouponItemDto(ProductId productId, int quantity, Money unitPrice) {
}