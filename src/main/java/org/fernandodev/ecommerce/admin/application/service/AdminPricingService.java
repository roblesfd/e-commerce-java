package org.fernandodev.ecommerce.admin.application.service;

import org.fernandodev.ecommerce.admin.application.port.in.AdminPricingUseCases;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.pricing.application.dto.AddPriceOverrideCommand;
import org.fernandodev.ecommerce.pricing.application.dto.DefinePriceCommand;
import org.fernandodev.ecommerce.pricing.application.dto.UpdatePriceCommand;
import org.fernandodev.ecommerce.pricing.application.port.in.PricingUseCases;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceOverrideId;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Orquestación administrativa de precios e historial.
 */
@RequiredArgsConstructor
public class AdminPricingService implements AdminPricingUseCases {

    private final PricingUseCases pricing;
    private final AuditService audit;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Price defineBasePrice(DefinePriceCommand command) {
        audit.record("DEFINE_PRICE", "price", command.productId().value(),
                "Precio base definido para " + command.productId());
        return pricing.defineBasePrice(command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Price updateBasePrice(PriceId id, UpdatePriceCommand command) {
        audit.record("UPDATE_PRICE", "price", id.value(), "Precio base actualizado");
        return pricing.updateBasePrice(id, command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Price addOverride(PriceId id, AddPriceOverrideCommand command) {
        audit.record("ADD_PRICE_OVERRIDE", "price", id.value(), "Override de precio añadido");
        return pricing.addOverride(id, command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Price removeOverride(PriceId id, PriceOverrideId overrideId) {
        audit.record("REMOVE_PRICE_OVERRIDE", "price", id.value(), "Override de precio eliminado");
        return pricing.removeOverride(id, overrideId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Price getPriceByProduct(ProductId productId, ProductVariantId variantId) {
        return pricing.getByProduct(productId, variantId);
    }
}
