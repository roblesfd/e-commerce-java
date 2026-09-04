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

    /**
     * Persiste el precio.
     */
    Price save(Price price);

    /**
     * Consulta un precio por identificador.
     */
    Optional<Price> findById(PriceId id);

    /**
     * Consulta el precio de un producto/variante.
     */
    Optional<Price> findByProductAndVariant(ProductId productId, ProductVariantId variantId);

    /**
     * Indica si existe un precio para el producto/variante.
     */
    boolean existsByProductAndVariant(ProductId productId, ProductVariantId variantId);
}