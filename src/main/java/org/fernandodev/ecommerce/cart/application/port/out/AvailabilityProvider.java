package org.fernandodev.ecommerce.cart.application.port.out;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Proveedor de disponibilidad de stock (contexto Inventory).
 */
public interface AvailabilityProvider {

    boolean available(ProductId productId, ProductVariantId variantId, int quantity);
}