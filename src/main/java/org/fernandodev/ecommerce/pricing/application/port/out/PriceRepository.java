package org.fernandodev.ecommerce.pricing.application.port.out;

import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;

/**
 * Puerto de salida de persistencia del agregado {@link Price}.
 */
public interface PriceRepository {

    Price save(Price price);

    Optional<Price> findById(PriceId id);

    Optional<Price> findByProductAndVariant(ProductId productId, ProductVariantId variantId);

    boolean existsByProductAndVariant(ProductId productId, ProductVariantId variantId);
}