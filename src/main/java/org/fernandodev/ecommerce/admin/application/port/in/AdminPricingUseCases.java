package org.fernandodev.ecommerce.admin.application.port.in;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.application.dto.AddPriceOverrideCommand;
import org.fernandodev.ecommerce.pricing.application.dto.DefinePriceCommand;
import org.fernandodev.ecommerce.pricing.application.dto.UpdatePriceCommand;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverrideId;

/**
 * Orquestación administrativa de precios e historial.
 */
public interface AdminPricingUseCases {

    Price defineBasePrice(DefinePriceCommand command);

    Price updateBasePrice(PriceId id, UpdatePriceCommand command);

    Price addOverride(PriceId id, AddPriceOverrideCommand command);

    Price removeOverride(PriceId id, PriceOverrideId overrideId);

    Price getPriceByProduct(ProductId productId, ProductVariantId variantId);
}