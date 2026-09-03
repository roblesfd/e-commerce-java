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

    Price defineBasePrice(DefinePriceCommand command);

    Price updateBasePrice(PriceId id, UpdatePriceCommand command);

    Price getById(PriceId id);

    Price getByProduct(ProductId productId, ProductVariantId variantId);

    Price addOverride(PriceId id, AddPriceOverrideCommand command);

    Price removeOverride(PriceId id, PriceOverrideId overrideId);

    Money currentPrice(ProductId productId, ProductVariantId variantId, Instant at);

    Money discount(ProductId productId, ProductVariantId variantId, Instant at);

    PriceSnapshot priceSnapshot(ProductId productId, ProductVariantId variantId, Instant at);
}