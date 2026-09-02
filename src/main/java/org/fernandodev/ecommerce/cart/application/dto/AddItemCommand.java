package org.fernandodev.ecommerce.cart.application.dto;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Datos para añadir un item al carrito.
 *
 * @param productId producto.
 * @param variantId variante (null si el item es a nivel producto).
 * @param quantity  cantidad solicitada.
 */
public record AddItemCommand(ProductId productId, ProductVariantId variantId, int quantity) {
}