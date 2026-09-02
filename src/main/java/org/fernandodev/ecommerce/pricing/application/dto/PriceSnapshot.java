package org.fernandodev.ecommerce.pricing.application.dto;

import java.time.Instant;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Instantánea del precio aplicable a un producto/variante en un instante.
 * <p>
 * Pensada para que el bounded context Order capture el precio al crear una
 * orden y garantizar consistencia histórica (spec §23).
 * </p>
 *
 * @param productId   producto.
 * @param variantId   variante (null si el precio es a nivel producto).
 * @param basePrice   precio base.
 * @param currentPrice precio vigente en {@code at}.
 * @param discount    descuento aplicado (base − vigente).
 * @param at          instante de la instantánea.
 */
public record PriceSnapshot(
        ProductId productId,
        ProductVariantId variantId,
        Money basePrice,
        Money currentPrice,
        Money discount,
        Instant at
) {
}