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

/**
     * Define el precio base y lo registra en auditoría.
     */
    Price defineBasePrice(DefinePriceCommand command);

/**
     * Actualiza el precio base y lo registra en auditoría.
     */
    Price updateBasePrice(PriceId id, UpdatePriceCommand command);

/**
     * Añade un override de precio y lo registra en auditoría.
     */
    Price addOverride(PriceId id, AddPriceOverrideCommand command);

/**
     * Elimina un override de precio y lo registra en auditoría.
     */
    Price removeOverride(PriceId id, PriceOverrideId overrideId);

/**
     * Consulta el precio y su historial por producto/variante.
     */
    Price getPriceByProduct(ProductId productId, ProductVariantId variantId);
}