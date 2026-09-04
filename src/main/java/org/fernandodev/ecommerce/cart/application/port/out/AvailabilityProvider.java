package org.fernandodev.ecommerce.cart.application.port.out;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Proveedor de disponibilidad de stock (contexto Inventory).
 */
public interface AvailabilityProvider {

    /**
     * @param productId producto.
     * @param variantId variante (null si el item es a nivel producto).
     * @param quantity  cantidad solicitada.
     * @return {@code true} si hay stock disponible para la cantidad pedida.
     */
    boolean available(ProductId productId, ProductVariantId variantId, int quantity);
}