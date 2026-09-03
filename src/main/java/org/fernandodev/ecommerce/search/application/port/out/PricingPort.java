package org.fernandodev.ecommerce.search.application.port.out;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Puerto hacia el contexto Pricing para el precio vigente del producto.
 */
public interface PricingPort {

    Money currentPrice(ProductId productId, ProductVariantId variantId);
}