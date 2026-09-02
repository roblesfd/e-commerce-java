package org.fernandodev.ecommerce.promotion.application.dto;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Línea del carrito aportada por el caller para evaluar promociones.
 */
public record CartItemDto(ProductId productId, int quantity, Money unitPrice) {
}