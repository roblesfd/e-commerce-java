package org.fernandodev.ecommerce.pricing.application.port.in;

import java.time.Instant;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.application.dto.AddPriceOverrideCommand;
import org.fernandodev.ecommerce.pricing.application.dto.DefinePriceCommand;
import org.fernandodev.ecommerce.pricing.application.dto.PriceSnapshot;
import org.fernandodev.ecommerce.pricing.application.dto.UpdatePriceCommand;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverrideId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Casos de uso del contexto Pricing (puerto de entrada).
 */
public interface PricingUseCases {

    /**
     * Define el precio base de un producto/variante.
     */
    Price defineBasePrice(DefinePriceCommand command);

    /**
     * Actualiza el precio base y registra el anterior en el historial.
     */
    Price updateBasePrice(PriceId id, UpdatePriceCommand command);

    /**
     * Consulta un precio por identificador.
     */
    Price getById(PriceId id);

    /**
     * Consulta el precio de un producto/variante (con historial).
     */
    Price getByProduct(ProductId productId, ProductVariantId variantId);

    /**
     * Añade un override de precio.
     */
    Price addOverride(PriceId id, AddPriceOverrideCommand command);

    /**
     * Elimina un override de precio.
     */
    Price removeOverride(PriceId id, PriceOverrideId overrideId);

    /**
     * Devuelve el precio vigente en el instante dado.
     */
    Money currentPrice(ProductId productId, ProductVariantId variantId, Instant at);

    /**
     * Devuelve el descuento aplicado en el instante dado.
     */
    Money discount(ProductId productId, ProductVariantId variantId, Instant at);

    /**
     * Devuelve la instantánea del precio en el instante dado (para capturas de orden).
     */
    PriceSnapshot priceSnapshot(ProductId productId, ProductVariantId variantId, Instant at);
}