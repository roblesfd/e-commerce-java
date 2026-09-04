package org.fernandodev.ecommerce.checkout.application.port.out;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Proveedor del precio vigente (contexto Pricing).
 */
public interface PriceProvider {

/**
     * @param productId producto.
     * @param variantId variante (null si es a nivel producto).
     * @return el precio vigente del producto/variante.
     */
    Money currentPrice(ProductId productId, ProductVariantId variantId);
}