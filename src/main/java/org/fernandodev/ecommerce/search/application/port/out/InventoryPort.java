package org.fernandodev.ecommerce.search.application.port.out;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Puerto hacia el contexto Inventory para sincronizar la disponibilidad.
 */
public interface InventoryPort {

    int availableStock(ProductId productId, ProductVariantId variantId);
}