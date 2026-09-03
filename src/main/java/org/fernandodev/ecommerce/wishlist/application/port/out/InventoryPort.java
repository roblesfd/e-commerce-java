package org.fernandodev.ecommerce.wishlist.application.port.out;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Puerto hacia el contexto Inventory: consultar stock disponible para detectar
 * variantes no disponibles.
 */
public interface InventoryPort {

    int availableStock(ProductId productId, ProductVariantId variantId);
}