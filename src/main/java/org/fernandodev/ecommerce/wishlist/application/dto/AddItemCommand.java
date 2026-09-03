package org.fernandodev.ecommerce.wishlist.application.dto;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Datos para añadir un item a una wishlist.
 */
public record AddItemCommand(ProductId productId, ProductVariantId variantId) {
}